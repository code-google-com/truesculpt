package truesculpt.tools;

import truesculpt.main.Managers;

public class ToolsBase implements ITools
{
	private Managers mManagers = null;

	public ToolsBase(Managers managers)
	{
		mManagers = managers;
	}

	public void Start()
	{

	}

	public void Pick(int nIndex)
	{

	}

	public void Stop()
	{

	}

	public Managers getManagers()
	{
		return mManagers;
	}

}