package me.anon.model;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class NoteAction extends Action
{
	public NoteAction()
	{
	}

	public NoteAction(String note)
	{
		this.setDate(System.currentTimeMillis());
		this.setNotes(note);
	}
}
