// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.client.view;

import java.util.Arrays;

import org.cloudcoder.app.client.model.Session;
import org.cloudcoder.app.client.model.StatusMessage;
import org.cloudcoder.app.client.page.SessionObserver;
import org.cloudcoder.app.client.rpc.RPC;
import org.cloudcoder.app.shared.model.Course;
import org.cloudcoder.app.shared.model.User;
import org.cloudcoder.app.shared.util.Publisher;
import org.cloudcoder.app.shared.util.Subscriber;
import org.cloudcoder.app.shared.util.SubscriptionRegistrar;

import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * @author jaimespacco
 *
 */
public class UserAdminUsersListView extends ResizeComposite implements Subscriber, SessionObserver
{
    private DataGrid<User> grid;
    private Session session;
    
    /**
     * Constructor.
     */
    public UserAdminUsersListView() {
        grid = new DataGrid<User>();
        grid.addColumn(new TextColumn<User>() {
            @Override
            public String getValue(User user) {
                return user.getUsername();
            }
        }, "Username");
        grid.addColumn(new TextColumn<User>() {
            @Override
            public String getValue(User user) {
                return user.getFirstname();
            }
        }, "Firstname");
        grid.addColumn(new TextColumn<User>() {
            @Override
            public String getValue(User user) {
                return user.getLastname();
            }
        }, "Lastname");
        grid.addColumn(new TextColumn<User>() {
            @Override
            public String getValue(User user) {
                return user.getEmail();
            }
        }, "Email");
        
        
        initWidget(grid);
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.client.page.SessionObserver#activate(org.cloudcoder.app.client.model.Session, org.cloudcoder.app.shared.util.SubscriptionRegistrar)
     */
    @Override
    public void activate(final Session session, SubscriptionRegistrar subscriptionRegistrar)
    {
        this.session = session;
        session.subscribe(Session.Event.ADDED_OBJECT, this, subscriptionRegistrar);
        
        // Set selection model.
        // When a User record is selected, it will be added to the Session.
        final SingleSelectionModel<User> selectionModel = new SingleSelectionModel<User>();
        selectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                User selected = selectionModel.getSelectedObject();
                if (selected != null) {
                    session.add(selected);
                }
            }
        });
        grid.setSelectionModel(selectionModel);
        
        // If the session contains a list of Users, display them.
        // Otherwise, initiate loading of users for this course.
        User[] userList = session.get(User[].class);
        //ProblemAndSubmissionReceipt[] problemAndSubmissionReceiptList = session.get(ProblemAndSubmissionReceipt[].class);
        if (userList != null) {
            displayUsers(userList);
        } else {
            Course course = session.get(Course.class);
            loadUsers(session, course);
        }
        
    }

    /* (non-Javadoc)
     * @see org.cloudcoder.app.shared.util.Subscriber#eventOccurred(java.lang.Object, org.cloudcoder.app.shared.util.Publisher, java.lang.Object)
     */
    @Override
    public void eventOccurred(Object key, Publisher publisher, Object hint) {
        if (key == Session.Event.ADDED_OBJECT && (hint instanceof Course)) {
            // Course selected, load its problems.
            // Note that this isn't really needed by CourseAdminPage (because there
            // is only one Course which is pre-selected), but if this view is
            // reused in another page at some point, this might be useful.
            loadUsers(session, (Course)hint);
        }
    }
    
    private void loadUsers(final Session session, Course course) {
        RPC.usersService.getUsers(course, new AsyncCallback<User[]>() {
            @Override
            public void onSuccess(User[] result) {
                displayUsers(result);
            }
            
            @Override
            public void onFailure(Throwable caught) {
                session.add(new StatusMessage(StatusMessage.Category.ERROR, "Could not load users for course"));
            }
        });
    }

    protected void displayUsers(User[] result) {
        grid.setRowCount(result.length);
        grid.setRowData(Arrays.asList(result));
    }
}