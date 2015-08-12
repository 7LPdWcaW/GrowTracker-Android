package me.anon.model;

import lombok.Getter;
import lombok.Setter;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Getter @Setter
public class NoteAction extends Action
{
	public NoteAction(String note)
	{
		this.setDate(System.currentTimeMillis());
		this.setNotes(note);
	}
}
