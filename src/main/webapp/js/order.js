//Stores logic required to work with order form
$(document).ready(function(){
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
                data[this.name] = this.value;I
            });
            return data;
        };

        var button = form.find('button');
        //TODO: implement values saving in the local storage
//        var modal = form.find('div#orderModal');
        button.click(function(){
            if (isFormValid()){
                alert('Will send message');
                $.ajax({
                    url: '/api/order',
                    data: JSON.stringify(getFormData()),
                    contentType: 'application/json; charset=utf-8',
                    dataType: 'json',
                    method: 'post',
                    success: function(){
                        //Do something
                    }
                });
            }else if (!isPhoneValid()){
                alert('Phone number not valid');
            } else if (!isClientNameValid()){
                alert('Client name not valid');
            }
            return false;
        });
    })();
});