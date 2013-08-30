/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.parser;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.workflow.kaleo.definition.Definition;
import com.liferay.portal.workflow.kaleo.definition.Fork;
import com.liferay.portal.workflow.kaleo.definition.Join;
import com.liferay.portal.workflow.kaleo.definition.Node;
import com.liferay.portal.workflow.kaleo.definition.NodeType;
import com.liferay.portal.workflow.kaleo.definition.Transition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael C. Han
 * @author Marcellus Tavares
 * @author Norbert Kocsis
 */
public class ForkNodeValidator extends BaseNodeValidator<Fork> {

	@Override
	protected void doValidate(Definition definition, Fork fork)
		throws WorkflowException {

		if (fork.getIncomingTransitionsCount() == 0) {
			throw new WorkflowException(
				"No incoming transition found for fork " + fork.getName());
		}

		if (fork.getOutgoingTransitionsCount() < 2) {
			throw new WorkflowException(
				"Less than 2 outgoing transitions found for fork " +
					fork.getName());
		}

		traverse(fork);
	}

	protected List<Node> getUnvisitedSourceNodes(
		List<Node> sourceNodes, Collection<Transition> incomingTransitions) {

		List<Node> unvisitedSourceNodes = new ArrayList<Node>();

		for (Transition incomingTransition : incomingTransitions) {
			Node sourceNode = incomingTransition.getSourceNode();

			if (!sourceNodes.contains(sourceNode)) {
				unvisitedSourceNodes.add(sourceNode);
			}
		}

		return unvisitedSourceNodes;
	}

	protected List<Node> getUnvisitedTargetNodes(
		List<Node> targetNodes, Collection<Transition> outgoingTransitions) {

		List<Node> unvisitedTargetNodes = new ArrayList<Node>();

		for (Transition outgoingTransition : outgoingTransitions) {
			Node targetNode = outgoingTransition.getTargetNode();

			if (!targetNodes.contains(targetNode)) {
				unvisitedTargetNodes.add(targetNode);
			}
		}

		return unvisitedTargetNodes;
	}

	protected List<Node> reverseTraverse(
		Fork fork, Join join, Map<Join, Fork> joinForkMap) {

		List<Node> sourceNodes = new ArrayList<Node>();

		sourceNodes.add(join);

		for (Transition transition : join.getIncomingTransitions()) {
			sourceNodes.add(transition.getSourceNode());
		}

		for (int i = 1; i < sourceNodes.size(); i++) {
			Node sourceNode = sourceNodes.get(i);

			if (sourceNode.getNodeType().equals(NodeType.JOIN)) {
				sourceNodes.set(i, joinForkMap.get((Join)sourceNode));
			}
			else if (Validator.equals(fork, sourceNode)) {
				continue;
			}

			List<Node> unvisitedSourceNodes = getUnvisitedSourceNodes(
				sourceNodes, sourceNode.getIncomingTransitions());

			sourceNodes.addAll(unvisitedSourceNodes);
		}

		return sourceNodes;
	}

	protected Join traverse(Fork fork) throws WorkflowException {
		Join join = null;

		List<Node> targetNodes = new ArrayList<Node>();

		Map<Join, Fork> joinForkMap = new HashMap<Join, Fork>();

		targetNodes.add(fork);

		for (Transition transition : fork.getOutgoingTransitionsList()) {
			targetNodes.add(transition.getTargetNode());
		}

		for (int i = 1; i < targetNodes.size(); i++) {
			Node targetNode = targetNodes.get(i);

			if (targetNode.getNodeType().equals(NodeType.FORK)) {
				Join localJoin = traverse((Fork)targetNode);

				joinForkMap.put(localJoin, (Fork)targetNode);

				List<Node> unvisitedTargetNodes = getUnvisitedTargetNodes(
					targetNodes, localJoin.getOutgoingTransitionsList());

				targetNodes.addAll(unvisitedTargetNodes);
			}
			else if (targetNode.getNodeType().equals(NodeType.JOIN)) {
				if (Validator.isNull(join)) {
					join = (Join)targetNode;
				}
				else if (!Validator.equals(join, targetNode)) {
					throw new WorkflowException(
						"Fork " + fork.getName() + " and join " +
							targetNode.getName() + " are not paired");
				}
			}
			else {
				List<Node> unvisitedTargetNodes = getUnvisitedTargetNodes(
					targetNodes, targetNode.getOutgoingTransitionsList());

				targetNodes.addAll(unvisitedTargetNodes);
			}
		}

		if (join == null) {
			throw new WorkflowException(
				"No matching join found for fork " + fork.getName());
		}

		List<Node> sourceNodes = reverseTraverse(fork, join, joinForkMap);

		if ((sourceNodes.size() != targetNodes.size()) ||
			!sourceNodes.containsAll(targetNodes)) {

			throw new WorkflowException("Invalid fork-join pair.");
		}

		return join;
	}

}