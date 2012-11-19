package org.traxnet.shadingzen.core2d;

import org.traxnet.shadingzen.core.Actor;
import org.traxnet.shadingzen.core.Engine;
import org.traxnet.shadingzen.core.RenderService;
import org.traxnet.shadingzen.math.Matrix4;
import org.traxnet.shadingzen.math.Vector2;
import org.traxnet.shadingzen.math.Vector3;

import android.opengl.Matrix;
import android.util.FloatMath;


/*
 * Order of transforms applied:
 * 	The node will be translated (position)
 * 	The node will be rotated (rotation)
 * 	The node will be scaled (scale)
 */
public class Node2d extends Actor /*implements Comparable*/ {
	protected float _nodeRotation;
	//protected Vector2 _nodePosition; /** Node position in normalized screen dimensions of 480x800 pixels */
	protected Vector2 _nodeAnchorPoint;
	protected Vector2 _contentSize;
	protected int _zOrder;
	protected float _nodeAlpha;
	protected Vector3 _nodeColor;
	
	protected float _nodeRotationSin = 0.f;
	protected float _nodeRotationCos = 0.f;
	
	protected Matrix4 _nodeCachedModelMatrix;
	protected boolean _nodeCachedModelMatrixIsDirty = true;
	
	protected boolean _nodeDrawInBackground = false;
	
	//private TreeSet<Node2d> _zorderedChildNodes;

	
	public Node2d(){
		setNodeRotation(0.f);
		_nodeColor = new Vector3(1.f, 1.f, 1.f);
		_nodeAnchorPoint = new Vector2(0.5f, 0.5f);
		_zOrder = 0;
		_contentSize = new Vector2();
		_nodeAlpha = 1.f;
		//_zorderedChildNodes = new TreeSet<Node2d>();
	}
	
	public void setNodeDrawInBackground(boolean value){
		_nodeDrawInBackground = value;
	}
	
	public boolean getNodeDrawInBackground(){
		return _nodeDrawInBackground;
	}
	
	public void setNodeColor(float r, float g, float b){
		_nodeColor.set(r, g, b);
	}
	
	public Vector3 getNodeColor(){
		return _nodeColor;
	}
	
	/** Set local position relative to parent actor */
	@Override
	public void setPosition(Vector3 v){
		_position = v;
		
		_nodeCachedModelMatrixIsDirty = true;
	}
	
	/*
	 * Sets node position in current screen dimensions
	 */
	public void setPositionInPixels(Vector2 position){
		float x = (position.getX()/Engine.getSharedInstance().getViewWidth())*480;
		float y = (position.getX()/Engine.getSharedInstance().getViewHeight())*800;
		_position.setX(x);
		_position.setY(y);
		
		_nodeCachedModelMatrixIsDirty = true;
	}
	
	/*
	 * Gets node position in current screen dimensions
	 */
	public Vector2 getPositionInPixels(){
		float x = (_position.getX()/480)*Engine.getSharedInstance().getViewWidth();
		float y = (_position.getX()/800)*Engine.getSharedInstance().getViewHeight();
		return new Vector2(x, y);
	}
	
	public void setZOrder(int z){
		_zOrder = z;
	}
	
	public void setNodeRotation(float rads){
		_nodeRotation = rads;
		
		_nodeRotationSin = FloatMath.sin(rads);
		_nodeRotationCos = FloatMath.cos(rads);
		
		_nodeCachedModelMatrixIsDirty = true;
	}
	
	public float getNodeRotation(){
		return _nodeRotation;
	}
	

	@Override
	public void setScale(float scale){
		_scale = scale;
		_nodeCachedModelMatrixIsDirty = true;
	}
	
	@Override
	public Matrix4 getLocalModelMatrix(){
		if(!_nodeCachedModelMatrixIsDirty)
			return _nodeCachedModelMatrix;
		
		float [] translate_matrix = {
				1.f, 0.f, 0.f, 0.f,
				0.f, 1.f, 0.f, 0.f,
				0.f, 0.f, 1.f, 0.f,
				_position.getX(), _position.getY(), 0.f, 1.f,
		};
		
		float [] anchor_matrix = {
				1.f, 0.f, 0.f, 0.f,
				0.f, 1.f, 0.f, 0.f,
				0.f, 0.f, 1.f, 0.f,
				-_nodeAnchorPoint.getX()*_contentSize.getX(), -_nodeAnchorPoint.getY()*_contentSize.getY(), 0.f, 1.f
				
		};
		
		float [] model_matrix = {
				_nodeRotationCos, _nodeRotationSin, 0.f, 0.f,
				-_nodeRotationSin,  _nodeRotationCos, 0.f, 0.f,
							 0.f,				0.f, 1.f, 0.f,
							 0.f,				0.f, 0.f, 1.f,
		};
		
		float [] scale_matrix = {
				_scale, 0.f, 0.f, 0.f,
				0.f, _scale, 0.f, 0.f,
				0.f, 0.f, 1.f, 0.f, 
				0.f, 0.f, 0.f, 1.f, 
		};
		
		float [] scale_rot = new float[16];
		float [] scale_step = new float[16];
		float [] ret = new float[16];
	
		
		Matrix.multiplyMM(scale_rot, 0, scale_matrix, 0,anchor_matrix, 0);
		Matrix.multiplyMM(scale_step, 0, model_matrix, 0,scale_rot, 0);
		Matrix.multiplyMM(ret, 0, translate_matrix, 0,scale_step, 0);
		
		_nodeCachedModelMatrix = new Matrix4(ret);
		
		_nodeCachedModelMatrixIsDirty = false;
		return _nodeCachedModelMatrix;
	}
	
	
	/*
	public float[] getModelMatrix(){
		float [] anchor_matrix = {
				1.f, 0.f, 0.f, 0.f,
				0.f, 1.f, 0.f, 0.f,
				0.f, 0.f, 0.f, 0.f,
				-_nodeAnchorPoint.getX()*_contentSize.getX(), -_nodeAnchorPoint.getY()*_contentSize.getY(), 0.f, 1.f
				
		};
		
		float [] model_matrix = {
				_nodeRotationCos, -_nodeRotationSin, 0.f, 0.f,
				_nodeRotationSin,  _nodeRotationCos, 0.f, 0.f,
							 0.f,				0.f, 1.f, 0.f,
							 _nodePosition.getX(),				_nodePosition.getY(), 0.f, 1.f,
		};
		
		float [] ret = new float[16];
		
		Matrix.multiplyMM(ret, 0, model_matrix, 0,anchor_matrix, 0);
		
		return ret;
	}*/
	
	public void setContentSize(Vector2 v){
		_contentSize.set(v);
	}
	
	public void setContentSize(float x, float y){
		_contentSize.setX(x);
		_contentSize.setY(y);
	}
	
	public Vector2 getContentSize(){
		return _contentSize;
	}
	
	public Vector2 getContentSizeInPixels(){
		float x = (_contentSize.getX()/640)*Engine.getSharedInstance().getViewWidth();
		float y = (_contentSize.getX()/800)*Engine.getSharedInstance().getViewHeight();
		return new Vector2(x, y);
	}
	
	public void setContentSizeInPixels(Vector2 size){
		float x = (_contentSize.getX()/Engine.getSharedInstance().getViewWidth())*640;
		float y = (_contentSize.getX()/Engine.getSharedInstance().getViewHeight())*800;
		_contentSize.set(x, y);
	}
	
	public float getNodeAlpha(){
		return _nodeAlpha;
	}
	
	public void setNodeAlpha(float alpha){
		_nodeAlpha = alpha;
	}

	@Override
	public void onUpdate(float delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDraw(RenderService renderer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnload() {
		// TODO Auto-generated method stub

	}
	/*
	public void addChild(Node2d child, int zorder){
		child._zOrder = zorder;
		
		_zorderedChildNodes.add(child);
	}

	@Override
	public int compareTo(Object another) {
		if(Node2d.class.isInstance(another)){
			Node2d another_node = (Node2d)another;
			
			return another_node._zOrder - _zOrder;
		}
			
		return 0;
	}*/

}
