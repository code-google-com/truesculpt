package truesculpt.actions;

import truesculpt.main.R;

public class POVOffsetRotationAction extends BaseAction
{

	public POVOffsetRotationAction(String strDescription)
	{
		super(strDescription);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean DoAction()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String GetActionName()
	{
		return "Rotate point of view";
	}

	@Override
	public int GetImageResourceID()
	{
		return R.drawable.resize;
	}

	@Override
	public boolean UndoAction()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
