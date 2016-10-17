package com.fdt.reddit.task.actions;

import com.fdt.reddit.task.actions.impl.*;


public enum RedditActions {
	ACTION_COMMENT(new RedditActionComment()),
	ACTION_LIKE_POST(new RedditActionLikePost()),
	ACTION_LIKE_COMMENT(new RedditActionLikeComment()),
	ACTION_JOIN_GROUP(new RedditActionJoinGroup());
	
	
	private IRedditActionProcessor processor;
	
	private RedditActions(IRedditActionProcessor processor){
		this.processor = processor;
	}
	
	public void execute(){
		processor.executeAction();
	}
	
}
