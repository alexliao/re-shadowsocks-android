package com.biganiseed.reindeer;

import com.biganiseed.reindeer.VpnConnector.State;


public abstract class OnStateChanged{
//		public State state;
//		
//		public OnStateChanged(State aState){
//			state = aState;
//		}
		public OnStateChanged(){}
		abstract public void run(State aState);
	}