jQuery(document).ready(function() {
    //Making top navigation responsive
    (function(){
        var optionsList = '<option value="" selected>Навигация по сайту</option>';

        $('.main-nav').find('li').each(function() {
            var $this   = $(this),
                $anchor = $this.children('a'),
                depth   = $this.parents('ul').length - 1,
                indent  = '';
            if( depth ) {
                while( depth > 0 ) {
                    indent += ' - ';
                    depth--;
                }
            }
            optionsList += '<option value="' + $anchor.attr('href') + '">' + indent + ' ' + $anchor.text() + '</option>';
        }).end().last()
            .after('<select class="responsive-nav">' + optionsList + '</select>');

        $('.responsive-nav').on('change', function() {
            window.location = $(this).val();
        });
    })();

    //Adding click handlers to tab panels of service pages
    (function(){
        $('#myTab').find('a').click(function(e) {
            e.preventDefault();
            $(this).tab('show');
        });
    })();

    //Adding click handlers to bundles toggle details links
    (function(){
        $('.bundle').find('a.toggleDetails').click(function(e) {
            $(this).parent().next('.details').slideToggle();
        });
    })();

});

