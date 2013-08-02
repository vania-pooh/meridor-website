// Responsive Nav

    var $mainNav    = $('.main-nav');
    var optionsList = '<option value="" selected>Навигация по сайту</option>';

    $mainNav.find('li').each(function() {
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
// end

//Adding click handlers to tab panels of service pages
jQuery(document).ready(function() {
    $('#myTab a').click(function (e) {
        e.preventDefault();
        $(this).tab('show');
    })
})


