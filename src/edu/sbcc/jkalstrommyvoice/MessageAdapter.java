/**
 * 
 */
package edu.sbcc.jkalstrommyvoice;

import java.util.*;

import android.app.*;
import android.view.*;
import android.widget.*;

/**
 * @author john.kalstrom
 * 
 */
public class MessageAdapter extends ArrayAdapter<Message> {

	Activity context;
	int layoutResourceId;
	List<Message> messages = null;

	public MessageAdapter(Activity context, int layoutResourceId, List<Message> messages) {
		super(context, layoutResourceId, messages);
		this.layoutResourceId = layoutResourceId;
		this.context = context;
		this.messages = messages;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the course that correponds to the row given by position.
		Message message = messages.get(position);

		// Inflate our row layout into the view that that will be returned by this function.
		View rowView = context.getLayoutInflater().inflate(layoutResourceId, null);

		// Update the labels
		((TextView) rowView.findViewById(R.id.callerIDLabel)).setText(message.callerID);
		((TextView) rowView.findViewById(R.id.dateLabel)).setText(message.date);
		((TextView) rowView.findViewById(R.id.transcriptionLabel)).setText(message.speechToText);

		return rowView;
	}

}
