describe( 'The JQuery Mobile AddressBook', function() {

	it( 'supports CRUD operations', function() {
		
		var _done = false;

		expect( $( 'article h1' ).text() ).toBe( 'People' );

		$( document ).one( 'pageshow', '#summary-page', function( event ) {

			var page = $( event.target );
			var summary = page.find( '#summary' );
			expect( summary.find( 'li:eq(0) a' ).text() ).toBe( 'Homer Simpson' );
			expect( summary.find( 'li:eq(1) a' ).text() ).toBe( 'Marge Simpson' );
			summary.find( 'li:eq(0) a' ).click();

			$( document ).one( 'pageshow', '#detail-page', function( event ) {
				
				page = $( event.target );
				var mw = page.find( '#metawidget' );
				expect( mw.find( '#firstname' )[0].tagName ).toBe( 'OUTPUT' );
				expect( mw.find( '#firstname' ).text() ).toBe( 'Homer' );
				expect( mw.find( '#surname' )[0].tagName ).toBe( 'OUTPUT' );
				expect( mw.find( '#surname' ).text() ).toBe( 'Simpson' );
				
				page.find( '#viewEdit' ).click();
				
				setTimeout( function() {

					expect( mw.find( '#firstname' )[0].tagName ).toBe( 'INPUT' );
					expect( mw.find( '#firstname' ).val() ).toBe( 'Homer' );
					expect( mw.find( '#surname' )[0].tagName ).toBe( 'INPUT' );
					expect( mw.find( '#surname' ).val() ).toBe( 'Simpson' );
					
					mw.find( '#firstname' ).val( 'Homer Jay' );	
					page.find( '#editUpdate' ).click();
	
					$( document ).one( 'pageshow', '#summary-page', function( event ) {
					
						page = $( event.target );
						summary = page.find( '#summary' );
						expect( summary.find( 'li:eq(0) a' ).text() ).toBe( 'Homer Jay Simpson' );
						expect( summary.find( 'li:eq(1) a' ).text() ).toBe( 'Marge Simpson' );
		
						_done = true;
					} );
				}, 1000 );
			} );			
		} );

		waitsFor( function() {

			return _done;
		});
	} );
} );