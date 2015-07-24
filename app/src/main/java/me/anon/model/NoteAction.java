package me.anon.model;

import lombok.Data;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
public class NoteAction extends Action
{
	public NoteAction(String note)
	{
		this.setDate(System.currentTimeMillis());
		this.setNotes(note);
	}
}
