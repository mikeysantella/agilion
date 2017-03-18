$(document).ready(function() {
    $('#sessionTable').DataTable();
    $("#dataJobTable").DataTable({searching: false, paging: false});

    $('#sessionTable').on('click', '.clickable-row', function(event) {
      $(this).addClass('active').siblings().removeClass('active');
      $("#sessionPanelContainer").removeClass("hidden");
    });

    $(".sticky-top-alert").hide();
} );

function submit()
{
    $(".sticky-top-alert").show();
}