function byName() {
	$(document).ready(function () {
		$('#myInput').on('keyup', function () {
			var value = $(this).val().toLowerCase();
			$('#myTable tr').filter(function () {
				$(this).toggle($(this).find('td:eq(1)').text().toLowerCase().indexOf(value) > -1)
			});
		});
	});
};