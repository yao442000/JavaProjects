package com.yinger.patientims.editors.editorInput;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class DiagnoseInfoEditorInput implements IEditorInput {

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return "Diagnose Information Management";
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Hospital Management/Diagnose Information Management";//�������ֵķ���ֵ�Ҿ���Ӧ�ø��Ӻ�������ƣ���ҪӲ���룡
	}

}