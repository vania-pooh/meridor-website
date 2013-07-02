/**
 * Contains a script for price calculator
 * Prices table is marked with "prices" class. Every price row of the table (tr) is marked with "price" class.
 * A cell with price total is marked with "total" class. A cell with sum of total prices of all tables is marked with "totalPrice" id.
 * The following classes are used in the table to mark individual cells:
 *  title - price title
 *  uom - price unit of measure like meters, hours, etc.
 *  unit_price - price value for 1 uom
 *  quantity - total order quantity for this price, i.e. how many uoms need to be done
 *  value - total amount of money to be paid for this price
 */
(function($){
    $.fn.createCalculator = function(){
        var tables = this;

        tables.each(function(){
            var currentTable = $(this);
            $('tr.price', currentTable).each(function(){
                var priceRow = $(this);

                //Input field change handler
                $('input.quantity', priceRow).change(function(){
                    updateRowValue($(this), priceRow);
                    updateTableTotal(currentTable);
                    updateGlobalTotal();
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
                });
            });

            //Global clear button click handler
            $('th a.i_clear', currentTable).click(function(){
                $('input.quantity', currentTable).val(0);
                $('td.value', currentTable).html(0);
                updateTableTotal(currentTable);
                updateGlobalTotal();
            });
        });
        return this;
    };

    //Updating row value cell
    var updateRowValue = function(quantityField, row){
        var quantityFieldVal = quantityField.val();
        var quantity = (!!quantityFieldVal && (quantityFieldVal > 0)) ? quantityFieldVal : 0;
        if (!quantityFieldVal){
            quantityField.val(0);
        }
        var unitPriceCell = $('td.unit_price', row);
        var unitPrice = (!!unitPriceCell.html()) ? unitPriceCell.html() : 0;
        var valueCell = $('td.value', row);
        valueCell.html(parseFloat(unitPrice * quantity));
    };

    //Updates total price of a single table
    var updateTableTotal = function(table){
        var perTableTotalPrice = 0;
        $('td.value', table).each(function(){
            perTableTotalPrice += parseFloat($(this).html());
        });
        var tableTotalPriceCell = $('td.total', table);
        tableTotalPriceCell.html(perTableTotalPrice);
    };

    //Updates sum of total prices at the bottom of the table
    var updateGlobalTotal = function(){
        var totalPriceCell = $('td#totalPrice');
        if (totalPriceCell.length > 0){
            var globalTotalPrice = 0;
            $('table.prices td.total').each(function(){
                globalTotalPrice += parseFloat($(this).html());
            });
            totalPriceCell.html(globalTotalPrice);
        }
    };
})(jQuery);

//Creating calculator for all existing price tables on the page
jQuery(document).ready(function(){
    $('table.prices').createCalculator();
});
