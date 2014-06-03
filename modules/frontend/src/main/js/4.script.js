jQuery(document).ready(function() {
    //Making top navigation responsive
    (function(){
        var optionsList = '<option value="" selected>Навигация по сайту</option>';
        var previousDepth = 0;
        $('.main-nav').find('li').each(function() {
            var $this   = $(this),
                $anchor = $this.children('a'),
                href = $anchor.attr('href'),
                depth   = $this.parents('ul').length - 1,
                label = $anchor.text();
            if (!!href){
                if (depth < previousDepth){
                    optionsList += '</optgroup>';
                }
                optionsList += '<option value="' + href + '">' + label + '</option>';
            } else {
                optionsList += '<optgroup label="' + label + '">';
            }
            previousDepth = depth;
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

