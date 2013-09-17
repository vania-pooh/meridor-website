//Stores logic required to work with order form
$(document).ready(function(){
    //Initializing alert panels
    (function(){
        $('#alert-loading').easyModal();
        $('#alert-ok').easyModal();
        $('#alert-submit-error').easyModal();
        $('#alert-invalid-phone').easyModal();
        $('#alert-invalid-client-name').easyModal();
    })();

    //Initializing dots animation
    (function(){
        var dots = 0;
        var dotsContainer = $('#alert-loading').find('span.dots');
        setInterval(function(){
            if(dots < 3) {
                dotsContainer.append('.');
                dots++;
            } else {
                dotsContainer.html('');
                dots = 0;
            }
        }, 600);
    })();

    //Adding validation logic to order forms
    (function(){
        $('form.form-order').each(function(){
            var form = $(this);
            var phoneField = form.find('input.phone');
            var clientNameField = form.find('input.client_name');
            var button = form.find('button');

            var isPhoneValid = function(){
                var phone = getFormData()['phone'];
                return /^(\+7|8)(\d{10})$/.exec(phone);
            };
            var isClientNameValid = function(){
                var clientName = getFormData()['client_name'];
                return (clientName.toString().length > 0);
            };
            var isFormValid = function(){
                return isPhoneValid() && isClientNameValid();
            };

            var getFormData = function(){
                var data = {};
                $.each(form.serializeArray(), function(){
                    data[this.name] = this.value;
                });
                return data;
            };

            var getStorageKey = function(fieldName){
                return 'mr_order_form' + fieldName;
            };

            var saveFormDataToStorage = function(formData){
                if (!!$.getStorage()){
                    var storage = $.getStorage();
                    for (var name in formData){
                        var value = formData[name];
                        if (
                            !!value
                            && ( (name == 'phone') || (name == 'client_name') )
                        ){
                            storage.setItem(getStorageKey(name), value);
                        }
                    }
                }
            };

            var loadFormDataFromStorage = function(){
                if (!!$.getStorage()){
                    var storage = $.getStorage();
                    var phone = storage.getItem(getStorageKey('phone'));
                    if (!!phone){
                        phoneField.val(phone);
                    }
                    var clientName = storage.getItem(getStorageKey('client_name'));
                    if (!!clientName){
                        clientNameField.val(clientName);
                    }
                }
            };

            //Submit button click handler
            button.click(function(){
                if (isFormValid()){
                    var formData = getFormData();
                    saveFormDataToStorage(formData);
                    $('#alert-loading').trigger('openModal');
                    $.ajax({
                        url: '/api/order',
                        data: JSON.stringify(formData),
                        contentType: 'application/json; charset=utf-8',
                        dataType: 'json',
                        method: 'post'
                    })
                    .done(function(data){
                        $('#alert-loading').trigger('closeModal');
                        if (!!data && !!data['code'] && (data['code'] == 'ok')){
                            $('#alert-ok').trigger('openModal');
                        } else {
                            $('#alert-submit-error').trigger('openModal');
                        }
                    })
                    .fail(function(){
                        $('#alert-loading').trigger('closeModal');
                        $('#alert-submit-error').trigger('openModal');
                    });
                }else if (!isPhoneValid()){
                    $('#alert-invalid-phone').trigger('openModal');
                } else if (!isClientNameValid()){
                    $('#alert-invalid-client-name').trigger('openModal');
                }
                return false;
            });

            loadFormDataFromStorage();

        });

    })();
});