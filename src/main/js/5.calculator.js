/**
 * Contains a script for price calculator
 * Prices table is marked with "prices" class. Every price row of the table (tr) is marked with "price" class.
 * A cell with price total is marked with "total" class. A cell with sum of total prices of all tables is marked
 * with "totalPrice" id. The following classes are used in the table to mark individual cells:
 *  id - price numeric ID
 *  title - price title
 *  uom - price unit of measure like meters, hours, etc.
 *  unit_price - price value for 1 uom
 *  quantity - total order quantity for this price, i.e. how many uoms need to be done
 *  value - total amount of money to be paid for this price
 */
(function($){
    $.fn.createCalculator = function(){
        var tables = this;

        //Updating row value cell
        var updateRowValue = function(quantityField, row){
            var quantityFieldVal = quantityField.val();
            var quantity = isPositiveNumber(quantityFieldVal) ? quantityFieldVal : 0;
            if (!quantityFieldVal){
                quantityField.val(0);
            }
            var unitPriceCell = $('td.unit_price', row);
            var unitPrice = stringToFloat(unitPriceCell.html());
            var valueCell = $('td.value', row);
            valueCell.html(formatPrice(parseFloat(unitPrice * quantity)));
        };

        //Whether this value is a positive integer
        var isPositiveNumber = function(value){
            return (!!value && (value > 0));
        };

        var stringToFloat = function(s){
            var price = parseFloat(s.replace(/\s|&nbsp;|,|'/g, ''));
            return !isNaN(price) ? price : 0;
        };

        var stringToInt = function(s){
            var price = parseInt(s);
            return !isNaN(price) ? price : 0;
        }

        var formatPrice = function(p){
            return p.toFixed(0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, " ");
        };

        //Updates total price of a single table
        var updateTableTotal = function(table){
            var perTableTotalPrice = 0;
            $('td.value', table).each(function(){
                perTableTotalPrice += stringToFloat($(this).html());
            });
            var tableTotalPriceCell = $('td.total', table);
            tableTotalPriceCell.html(formatPrice(perTableTotalPrice));
        };

        //Updates sum of total prices at the bottom of the table
        var updateGlobalTotal = function(){
            var totalPriceCell = $('td#totalPrice');
            var globalTotalPrice = 0;
            $('table.prices td.total').each(function(){
                globalTotalPrice += stringToFloat($(this).html());
            });
            if (totalPriceCell.length > 0){
                totalPriceCell.html(formatPrice(globalTotalPrice));
            }

            if (globalTotalPrice > 0){
                $('a#downloadSelectedPrices').removeClass('hidden');
            }else{
                $('a#downloadSelectedPrices').addClass('hidden');
            }
        };

        //Saves table state to available persistent storage.
        //We're storing (priceId; quantity) pairs list for each table.
        var saveTableStateToStorage = function(table){
            if (!!$.getStorage()){
                var tableState = {};
                foreachPriceRow(function(priceRow, priceId, quantity, priceCell, quantityField){
                    if (
                        isPositiveNumber(priceId)
                            && isPositiveNumber(quantity)
                        ){
                        tableState[priceId] = quantity;
                    }
                }, table);

                $.getStorage().setItem(
                    getStorageKey(table),
                    JSON.stringify(tableState)
                );
            }
        };

        //Loads table state from available persistent storage
        var loadTableStateFromStorage = function(table){
            if (!!$.getStorage()){
                var tableState = JSON.parse(
                    $.getStorage().getItem(
                        getStorageKey(table)
                    )
                );
                if (!!tableState){
                    foreachPriceRow(function(priceRow, priceId, quantity, priceCell, quantityField){
                        if (isPositiveNumber(priceId)){
                            var quantityFromStorage = tableState[priceId];
                            if (isPositiveNumber(quantityFromStorage)){
                                quantityField.val(quantityFromStorage);
                                updateRowValue(quantityField, priceRow);
                            }
                        }
                    }, table);
                    updateTableTotal(table);
                    updateGlobalTotal();
                }
            }
        };

        //Returns a key to be used for accessing storage property for this table
        var getStorageKey = function(table){
            return 'mr_price_' + table.attr('id');
        };

        var foreachPriceRow = function(fn, table){
            var priceRows = (!!table) ? $('tr.price', table) : $('tr.price');
            if (!!fn){
                priceRows.each(function(){
                    var priceRow = $(this);
                    var priceCell = $('span.id', priceRow);
                    var quantityField = $('input.quantity', priceRow);
                    if (
                        (priceCell.length == 1)
                        && (quantityField.length == 1)
                    ){
                        var priceId = priceCell.html();
                        var quantityFieldVal = quantityField.val();
                        fn(priceRow, priceId, quantityFieldVal, priceCell, quantityField);
                    }
                });
            }
        };

        //Adding event handlers and loading cells data
        tables.each(function(){
            var currentTable = $(this);
            $('tr.price', currentTable).each(function(){
                var priceRow = $(this);

                //Input field change handler
                $('input.quantity', priceRow).change(function(){
                    updateRowValue($(this), priceRow);
                    updateTableTotal(currentTable);
                    updateGlobalTotal();
                    saveTableStateToStorage(currentTable);
                });

                //Input field focus handler
                $('input.quantity', priceRow).focus(function(){
                    $(this).select();
                });

                //Clear button click handler
                $('a.i_clear', priceRow).click(function(){
                    $('input.quantity', priceRow).val(0);
                    $('td.value', priceRow).html(0);
                    updateTableTotal(currentTable);
                    updateGlobalTotal();
                    saveTableStateToStorage(currentTable);
                });
            });

            //Global clear button click handler
            $('th a.i_clear', currentTable).click(function(){
                $('input.quantity', currentTable).val(0);
                $('td.value', currentTable).html(0);
                updateTableTotal(currentTable);
                updateGlobalTotal();
                saveTableStateToStorage(currentTable);
            });

            //Fading duration
            var FADING_DURATION = 500;

            //Fade toggle table main categories
            $('caption', currentTable).click(function(){
                $('thead, tbody.services, tbody.subcategory_header, tfoot', currentTable).fadeToggle({
                    duration: FADING_DURATION,
                    start: function(){
                        if ($('tbody.subcategory_services:visible', currentTable).length > 0){
                            $('tbody.subcategory_services:visible', currentTable).fadeOut(FADING_DURATION);
                        }
                    }
                });
            });

            //Fade toggle table subcategories
            $('th.subcategory_header', currentTable).find('a').click(function(){
                $(this).parent().parent().parent().next('tbody.subcategory_services').fadeToggle(FADING_DURATION);
            });

            if (currentTable.hasClass('readOnly')){
                //Updating totals one time manually
                $('tr.price', currentTable).each(function(){
                    var priceRow = $(this);
                    var quantityInput = $('input.quantity', priceRow);
                    if (quantityInput.length > 0){
                        updateRowValue(quantityInput, priceRow);
                    }
                });
                updateTableTotal(currentTable);
                updateGlobalTotal();
            } else {
                //Loading cells data
                loadTableStateFromStorage(currentTable);
            }
        });

        //Download button click handlers
        var PDF_EXPORT_URL = '/api/prices/export/pdf';
        $('a#downloadPrices').attr('href', 'javascript:void(0)').click(function(e){
            e.preventDefault();
            window.location = PDF_EXPORT_URL;
        });

        $('a#downloadSelectedPrices').click(function(e){
            e.preventDefault();
            var priceIds = [];
            var quantities = [];
            foreachPriceRow(function(priceRow, priceId, quantity){
                if(quantity > 0){
                    priceIds[priceIds.length] = stringToInt(priceId);
                    quantities[quantities.length] = stringToFloat(quantity);
                }
            });
            var url = PDF_EXPORT_URL
                + '?data=' + JSON.stringify({"exportAllServices": false, "serviceIds": priceIds, "quantities": quantities});
            window.location.href = url;
        });

        return this;
    };

})(jQuery);

//Creating calculator for all existing price tables on the page
jQuery(document).ready(function(){
    $('table.prices').createCalculator();
});
