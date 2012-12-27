package org.metawidget.js.angular;

import org.metawidget.util.JavaScriptTestCase;

public class AddressBookAngularTest
	extends JavaScriptTestCase {

	//
	// Public methods
	//

	public void testAddressBook()
		throws Exception {

		evaluateHtml( "target/addressbook-angularjs/index.html" );
		run( "src/test/js/addressbook-angular-tests.js" );

		// Ideally we would use...
		//
		// evaluateHtml( "http://localhost:8080/addressbook-angularjs/test-runner.html" );
		//
		// ...and...
		//
		// evaluateString( "Envjs.wait(10000); print( document.innerHTML )" );
		//
		// ...here to run test-runner.html on the server. But that didn't work?
	}
}