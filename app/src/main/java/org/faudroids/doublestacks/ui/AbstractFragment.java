package org.faudroids.doublestacks.ui;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import roboguice.fragment.provided.RoboFragment;

abstract class AbstractFragment extends RoboFragment {

	private final int layoutResource;
	protected ActionListener actionListener = null;

	AbstractFragment(int layoutResource) {
		this.layoutResource = layoutResource;
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(layoutResource, container, false);
	}


	@Override
	public void onActivityCreated(Bundle bundle) {
		super.onActivityCreated(bundle);
		try {
			actionListener = (ActionListener) getActivity();
		} catch (ClassCastException e) {
			throw new RuntimeException("activity must implement " + ActionListener.class.getName());
		}
	}

}
