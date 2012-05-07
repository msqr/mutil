/* ===================================================================
 * MailServer.java
 * 
 * Created May 7, 2012 3:16:16 PM
 * 
 * Copyright (c) 2012 Matt Magoffin.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id$
 * ===================================================================
 */

package magoffin.matt.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import javax.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.TooMuchDataException;
import org.subethamail.wiser.Wiser;

/**
 * Test mail server to aid unit testing.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class MailServer extends Wiser {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public MailServer() {
		super();
	}

	public MailServer(int port) {
		super(port);
	}

	/**
	 * Start the server.
	 * 
	 * <p>
	 * A single integer argument is allowed, for the port the SMTP server will
	 * listen on. Defaults to port 2500.
	 * </p>
	 * 
	 * @param args
	 *        the command line arguments
	 * @throws Exception
	 *         if any error occurs
	 */
	public static void main(String[] args) throws Exception {
		int port = 2500;
		if ( args.length > 0 ) {
			port = Integer.parseInt(args[0]);
		}
		MailServer server = new MailServer(port);
		server.start();
	}

	@Override
	public void deliver(String from, String recipient, InputStream data) throws TooMuchDataException,
			IOException {
		super.deliver(from, recipient, data);
		ByteArrayOutputStream byos = new ByteArrayOutputStream();
		try {
			dumpMessages(new PrintStream(byos));
			log.info("Mail from {} to {}:\n{}", new Object[] { from, recipient, byos.toString() });
		} catch ( MessagingException e ) {
			log.error("Exception printing mail from {} to {}", new Object[] { from, recipient }, e);
		}
		getMessages().clear();
	}

}
