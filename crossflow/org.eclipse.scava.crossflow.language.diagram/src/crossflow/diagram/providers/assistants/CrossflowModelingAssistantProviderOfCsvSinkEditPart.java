/*
 * 
 */
package crossflow.diagram.providers.assistants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;

import crossflow.diagram.edit.parts.CsvSinkEditPart;
import crossflow.diagram.edit.parts.QueueEditPart;
import crossflow.diagram.edit.parts.TopicEditPart;
import crossflow.diagram.providers.CrossflowElementTypes;
import crossflow.diagram.providers.CrossflowModelingAssistantProvider;

/**
 * @generated
 */
public class CrossflowModelingAssistantProviderOfCsvSinkEditPart extends CrossflowModelingAssistantProvider {

	/**
	* @generated
	*/
	@Override

	public List<IElementType> getRelTypesOnSource(IAdaptable source) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source.getAdapter(IGraphicalEditPart.class);
		return doGetRelTypesOnSource((CsvSinkEditPart) sourceEditPart);
	}

	/**
	* @generated
	*/
	public List<IElementType> doGetRelTypesOnSource(CsvSinkEditPart source) {
		List<IElementType> types = new ArrayList<IElementType>(2);
		types.add(CrossflowElementTypes.TaskInput_4002);
		types.add(CrossflowElementTypes.TaskOutput_4003);
		return types;
	}

	/**
	* @generated
	*/
	@Override

	public List<IElementType> getRelTypesOnSourceAndTarget(IAdaptable source, IAdaptable target) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source.getAdapter(IGraphicalEditPart.class);
		IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target.getAdapter(IGraphicalEditPart.class);
		return doGetRelTypesOnSourceAndTarget((CsvSinkEditPart) sourceEditPart, targetEditPart);
	}

	/**
	* @generated
	*/
	public List<IElementType> doGetRelTypesOnSourceAndTarget(CsvSinkEditPart source,
			IGraphicalEditPart targetEditPart) {
		List<IElementType> types = new LinkedList<IElementType>();
		if (targetEditPart instanceof TopicEditPart) {
			types.add(CrossflowElementTypes.TaskInput_4002);
		}
		if (targetEditPart instanceof QueueEditPart) {
			types.add(CrossflowElementTypes.TaskInput_4002);
		}
		if (targetEditPart instanceof TopicEditPart) {
			types.add(CrossflowElementTypes.TaskOutput_4003);
		}
		if (targetEditPart instanceof QueueEditPart) {
			types.add(CrossflowElementTypes.TaskOutput_4003);
		}
		return types;
	}

	/**
	* @generated
	*/
	@Override

	public List<IElementType> getTypesForTarget(IAdaptable source, IElementType relationshipType) {
		IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source.getAdapter(IGraphicalEditPart.class);
		return doGetTypesForTarget((CsvSinkEditPart) sourceEditPart, relationshipType);
	}

	/**
	* @generated
	*/
	public List<IElementType> doGetTypesForTarget(CsvSinkEditPart source, IElementType relationshipType) {
		List<IElementType> types = new ArrayList<IElementType>();
		if (relationshipType == CrossflowElementTypes.TaskInput_4002) {
			types.add(CrossflowElementTypes.Topic_2003);
			types.add(CrossflowElementTypes.Queue_2004);
		} else if (relationshipType == CrossflowElementTypes.TaskOutput_4003) {
			types.add(CrossflowElementTypes.Topic_2003);
			types.add(CrossflowElementTypes.Queue_2004);
		}
		return types;
	}

}
