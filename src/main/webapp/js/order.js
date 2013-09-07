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
        var form = $('form.form-order');

        var isPhoneValid = function(){
            var phoneField = form.find('input#phone');
            return /^(\+7|8)(\d{10})$/.exec(phoneField.val());
        };
        var isClientNameValid = function(){
            var clientNameField = form.find('input#client_name');
            return (clientNameField.val().toString().length > 0);
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

        var button = form.find('button');
        //TODO: implement values saving in the local storage
        button.click(function(){
            if (isFormValid()){
                $('#alert-loading').trigger('openModal');
                $.ajax({
                    url: '/api/order',
                    data: JSON.stringify(getFormData()),
                    contentType: 'application/json; charset=utf-8',
                    dataType: 'json',
                    method: 'post',
                    success: function(data){
                        $('#alert-loading').trigger('closeModal');
                        if (!!data && !!data['code'] && (data['code'] == 'ok')){
                            $('#alert-ok').trigger('openModal');
                        } else {
                            $('#alert-submit-error').trigger('openModal');
                        }
                    }
                });
            }else if (!isPhoneValid()){
                $('#alert-invalid-phone').trigger('openModal');
            } else if (!isClientNameValid()){
                $('#alert-invalid-client-name').trigger('openModal');
            }
            return false;
        });
    })();
});