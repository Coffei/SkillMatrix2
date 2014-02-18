jQuery(document).ready( function() {
    jQuery("button.rf-pick-add-all, button.rf-pick-add").append("<i class=\"icon-plus-sign\"/>");
    jQuery("button.rf-pick-rem-all, button.rf-pick-rem").append("<i class=\"icon-minus-sign\"/>");
});
jQuery(document).on('click.bs.dropdown.data-api', '.dropdown-menu input', function (e) { e.stopPropagation() });






