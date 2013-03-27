// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.app.server.persist.txn;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnable;
import org.cloudcoder.app.shared.model.CloudCoderAuthenticationException;
import org.cloudcoder.app.shared.model.Course;

/**
 * Transaction to insert users by reading an input stream.
 */
public class InsertUsersFromInputStream extends AbstractDatabaseRunnable<Boolean> {
	private final InputStream in;
	private final Course course;

	/**
	 * Constructor.
	 * 
	 * @param in     the input stream
	 * @param course the {@link Course} to which the users should be added
	 */
	public InsertUsersFromInputStream(InputStream in, Course course) {
		this.in = in;
		this.course = course;
	}

	@Override
	public Boolean run(Connection conn) throws SQLException,CloudCoderAuthenticationException
	{
		Queries.doInsertUsersFromInputStream(in, course, conn);
		return true;
	}

	@Override
	public String getDescription() {
		return "Inserting users into course "+course.getName();
	}
}