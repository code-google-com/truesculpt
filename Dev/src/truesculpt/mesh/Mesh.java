package truesculpt.mesh;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.microedition.khronos.opengles.GL10;
import truesculpt.main.Managers;
import truesculpt.utils.MatrixUtils;
import truesculpt.utils.Utils;

public class Mesh
{
	ArrayList<Edge> mEdgeList = new ArrayList<Edge>();
	ArrayList<Face> mFaceList = new ArrayList<Face>();
	ArrayList<Vertex> mVertexList = new ArrayList<Vertex>();
	ArrayList<RenderFaceGroup> mRenderGroupList = new ArrayList<RenderFaceGroup>();
	Managers mManagers;

	public Mesh(Managers managers)
	{
		mManagers = managers;

		InitAsSphere(3);

		// String strFileName=getManagers().getUtilsManager().CreateObjExportFileName();
		// ExportToOBJ(strFileName);

		mRenderGroupList.add(new RenderFaceGroup(this));
	}

	void ComputeAllVertexNormals()
	{
		int n = mVertexList.size();
		for (int i = 0; i < n; i++)
		{
			Vertex vertex = mVertexList.get(i);
			ComputeVertexNormal(vertex);
		}
	}
	
	float mBoundingSphereRadius = 0.0f;
	public void ComputeBoundingSphereRadius()
	{
		int n = mVertexList.size();
		for (int i = 0; i < n; i++)
		{
			Vertex vertex = mVertexList.get(i);
			float norm = MatrixUtils.magnitude(vertex.Coord);
			if (norm > mBoundingSphereRadius)
			{
				mBoundingSphereRadius = norm;
			}
		}
		getManagers().getPointOfViewManager().setRmin(1 + mBoundingSphereRadius);
	}

	// Based on close triangles normals * sin of their angle and normalize
	// averaging normals of triangles around
	void ComputeVertexNormal(Vertex vertex)
	{
		
	}
	
	//based on triangle only
	void ComputeFaceNormal(Face face, float[] normal)
	{
		// get triangle edge vectors and plane normal
		MatrixUtils.minus(mVertexList.get(face.V1).Coord, mVertexList.get(face.V0).Coord, u);
		MatrixUtils.minus(mVertexList.get(face.V2).Coord, mVertexList.get(face.V0).Coord, v);

		MatrixUtils.cross(u, v, n); // cross product
		MatrixUtils.normalize(n);

		MatrixUtils.copy(n, normal);
	}

	public void draw(GL10 gl)
	{
		for (RenderFaceGroup renderGroup : mRenderGroupList)
		{
			renderGroup.draw(gl);
		}
	}
	
	public void drawNormals(GL10 gl)
	{
		for (RenderFaceGroup renderGroup : mRenderGroupList)
		{
			renderGroup.drawNormals(gl);
		}
	}

	// From http://en.wikipedia.org/wiki/Wavefront_.obj_file
	void ExportToOBJ(String strFileName)
	{
		try
		{
			BufferedWriter file = new BufferedWriter(new FileWriter(strFileName));

			file.write("#Generated by TrueSculpt version " + getManagers().getUpdateManager().getCurrentVersion().toString() + "\n");
			file.write("http://code.google.com/p/truesculpt/\n");

			file.write("\n");
			file.write("# List of Vertices, with (x,y,z[,w]) coordinates, w is optional\n");
			for (Vertex vertex : mVertexList)
			{
				String str = "v " + String.valueOf(vertex.Coord[0]) + " " + String.valueOf(vertex.Coord[1]) + " " + String.valueOf(vertex.Coord[2]) + "\n";
				file.write(str);
			}

			file.write("\n");
			file.write("# Texture coordinates, in (u,v[,w]) coordinates, w is optional\n");
			file.write("\n");

			file.write("# Normals in (x,y,z) form; normals might not be unit\n");
			for (Vertex vertex : mVertexList)
			{
				String str = "vn " + String.valueOf(vertex.Normal[0]) + " " + String.valueOf(vertex.Normal[1]) + " " + String.valueOf(vertex.Normal[2]) + "\n";
				file.write(str);
			}

			file.write("\n");
			file.write("# Face Definitions\n");
			for (Face face : mFaceList)
			{
				int n0 = face.V0;
				int n1 = face.V1;
				int n2 = face.V2;

				assertTrue(n0 >= 0);
				assertTrue(n1 >= 0);
				assertTrue(n2 >= 0);

				// A valid vertex index starts from 1 and match first vertex element of vertex list previously defined. Each face can contain more than three elements.
				String str = "f " + String.valueOf(n0 + 1) + "//" + String.valueOf(n0 + 1) + " " + String.valueOf(n1 + 1) + "//" + String.valueOf(n1 + 1) + " " + String.valueOf(n2 + 1) + "//" + String.valueOf(n2 + 1) + "\n";

				file.write(str);
			}

			file.write("\n");
			file.close();

		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		getManagers().getUtilsManager().ShowToastMessage("Sculpture successfully exported to " + strFileName);
	}

	private void FinalizeInit()
	{
		// Set default vertex color
		int color = getManagers().getToolsManager().getColor();
		for (Vertex vertex : mVertexList)
		{
			vertex.Color = color;
		}

		// check triangle normals are outside and correct if necessary
		float[] u = new float[3];
		float[] v = new float[3];
		float[] n = new float[3];
		float[] dir = new float[3];

		for (Face face : mFaceList)
		{
			Vertex V0 = mVertexList.get(face.V0);
			Vertex V1 = mVertexList.get(face.V1);
			Vertex V2 = mVertexList.get(face.V2);

			// get triangle edge vectors and plane normal
			MatrixUtils.minus(V1.Coord, V0.Coord, u);
			MatrixUtils.minus(V2.Coord, V0.Coord, v);

			MatrixUtils.cross(u, v, n); // cross product

			dir = V0.Coord;

			boolean bCollinear = MatrixUtils.dot(dir, n) > 0;// dir and normal have same direction
			if (!bCollinear)// swap two edges
			{
				assertTrue(false);
			}
		}
	}

	

	public int getEdgeCount()
	{
		return mEdgeList.size();
	}

	public int getFaceCount()
	{
		return mFaceList.size();
	}

	public Managers getManagers()
	{
		return mManagers;
	}

	public int getVertexCount()
	{
		return mVertexList.size();
	}

	void ImportFromOBJ(String strFileName) throws IOException
	{
		Reset();

		int nCount = 0;

		LineNumberReader input = new LineNumberReader(new InputStreamReader(new FileInputStream(strFileName)));
		String line = null;
		try
		{
			for (line = input.readLine(); line != null; line = input.readLine())
			{
				if (line.length() > 0)
				{
					if (line.startsWith("v "))
					{
						float[] coord = new float[3];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						coord[0] = Float.parseFloat(tok.nextToken());
						coord[1] = Float.parseFloat(tok.nextToken());
						coord[2] = Float.parseFloat(tok.nextToken());
						mVertexList.add(new Vertex(coord));
					} else if (line.startsWith("vt "))
					{
						float[] coord = new float[2];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						coord[0] = Float.parseFloat(tok.nextToken());
						coord[1] = Float.parseFloat(tok.nextToken());
						// m.addTextureCoordinate(coord);
					} else if (line.startsWith("f "))
					{
						int[] face = new int[3];
						int[] face_n_ix = new int[3];
						int[] face_tx_ix = new int[3];
						int[] val;

						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						val = Utils.parseIntTriple(tok.nextToken());
						face[0] = val[0];
						if (val.length > 1 && val[1] > -1)
						{
							face_tx_ix[0] = val[1];
						}
						if (val.length > 2 && val[2] > -1)
						{
							face_n_ix[0] = val[2];
						}

						val = Utils.parseIntTriple(tok.nextToken());
						face[1] = val[0];
						if (val.length > 1 && val[1] > -1)
						{
							face_tx_ix[1] = val[1];
						}
						if (val.length > 2 && val[2] > -1)
						{
							face_n_ix[1] = val[2];
						}

						val = Utils.parseIntTriple(tok.nextToken());
						face[2] = val[0];
						if (val.length > 1 && val[1] > -1)
						{
							face_tx_ix[2] = val[1];
							// m.addTextureIndices(face_tx_ix);
						}
						if (val.length > 2 && val[2] > -1)
						{
							face_n_ix[2] = val[2];
							// m.addFaceNormals(face_n_ix);
						}
						mFaceList.add(new Face(face[0],face[1],face[2]));
						if (tok.hasMoreTokens())
						{
							val = Utils.parseIntTriple(tok.nextToken());
							face[1] = face[2];
							face[2] = val[0];
							if (val.length > 1 && val[1] > -1)
							{
								face_tx_ix[1] = face_tx_ix[2];
								face_tx_ix[2] = val[1];
								// m.addTextureIndices(face_tx_ix);
							}
							if (val.length > 2 && val[2] > -1)
							{
								face_n_ix[1] = face_n_ix[2];
								face_n_ix[2] = val[2];
								// m.addFaceNormals(face_n_ix);
							}
							mFaceList.add(new Face(face[0],face[1],face[2]));
						}

					} else if (line.startsWith("vn "))
					{
						nCount++;
						float[] norm = new float[3];
						StringTokenizer tok = new StringTokenizer(line);
						tok.nextToken();
						norm[0] = Float.parseFloat(tok.nextToken());
						norm[1] = Float.parseFloat(tok.nextToken());
						norm[2] = Float.parseFloat(tok.nextToken());
						// m.addNormal(norm);
					}
				}
			}
		} catch (Exception ex)
		{
			System.err.println("Error parsing file:");
			System.err.println(input.getLineNumber() + " : " + line);
		}
		// if (!file_normal) {
		// m.calculateFaceNormals(coordinate_hand);
		// m.calculateVertexNormals();
		// // m.copyNormals();
	}

	void InitAsIcosahedron()
	{
		float t = (float) ((1 + Math.sqrt(5)) / 2);
		float tau = (float) (t / Math.sqrt(1 + t * t));
		float one = (float) (1 / Math.sqrt(1 + t * t));

		mVertexList.add( new Vertex(tau, one, 0.0f));
		mVertexList.add( new Vertex(-tau, one, 0.0f));
		mVertexList.add( new Vertex(-tau, -one, 0.0f));
		mVertexList.add( new Vertex(tau, -one, 0.0f));
		mVertexList.add( new Vertex(one, 0.0f, tau));
		mVertexList.add( new Vertex(one, 0.0f, -tau));
		mVertexList.add( new Vertex(-one, 0.0f, -tau));
		mVertexList.add( new Vertex(-one, 0.0f, tau));
		mVertexList.add( new Vertex(0.0f, tau, one));
		mVertexList.add( new Vertex(0.0f, -tau, one));
		mVertexList.add( new Vertex(0.0f, -tau, -one));
		mVertexList.add( new Vertex(0.0f, tau, -one));

		// Counter clock wise (CCVW) face definition
		mFaceList.add( new Face(4, 8, 7));
		mFaceList.add( new Face(4, 7, 9));
		mFaceList.add( new Face(5, 6, 11));
		mFaceList.add( new Face(5, 10, 6));
		mFaceList.add( new Face(0, 4, 3));
		mFaceList.add( new Face(0, 3, 5));
		mFaceList.add( new Face(2, 7, 1));
		mFaceList.add( new Face(2, 1, 6));
		mFaceList.add( new Face(8, 0, 11));
		mFaceList.add( new Face(8, 11, 1));
		mFaceList.add( new Face(9, 10, 3));
		mFaceList.add( new Face(9, 2, 10));
		mFaceList.add( new Face(8, 4, 0));
		mFaceList.add( new Face(11, 0, 5));
		mFaceList.add( new Face(4, 9, 3));
		mFaceList.add( new Face(5, 3, 10));
		mFaceList.add( new Face(7, 8, 1));
		mFaceList.add( new Face(6, 1, 11));
		mFaceList.add( new Face(7, 2, 9));
		mFaceList.add( new Face(6, 10, 2));

		assertEquals(mFaceList.size(), 20);
		assertEquals(mVertexList.size(), 12);

		// n_vertices = 12;
		// n_faces = 20;
		// n_edges = 30;
	}

	void InitAsSphere(int nSubdivionLevel)
	{
		Reset();
		InitAsIcosahedron();
		//TODO create edges
		for (int i = 0; i < nSubdivionLevel; i++)
		{
			SubdivideAllFaces();			
		}		
		FinalizeInit();
		NormalizeAllVertices();
	}

	// makes a sphere
	void NormalizeAllVertices()
	{
		int n = mVertexList.size();
		for (int i = 0; i < n; i++)
		{
			Vertex vertex = mVertexList.get(i);
			MatrixUtils.normalize(vertex.Coord);
			MatrixUtils.copy(vertex.Coord, vertex.Normal);// Normal is coord because sphere is radius 1
		}
	}

	public int Pick(float[] rayPt1, float[] rayPt2, float [] intersectPtReturn)
	{
		int nRes = -1;

		float[] R0 = new float[3];
		float[] R1 = new float[3];
		float[] Ires = new float[3];

		MatrixUtils.copy(rayPt1, R0);
		MatrixUtils.copy(rayPt2, R1);

		MatrixUtils.minus(R1, R0, dir);
		float fSmallestDistanceToR0 = MatrixUtils.magnitude(dir);// ray is R0 to R1

		int nFaceCount = getFaceCount();
		for (int i=0;i<nFaceCount;i++)
		{
			Face face = mFaceList.get(i);
			
			int nCollide = intersect_RayTriangle(R0, R1, mVertexList.get(face.V0).Coord,  mVertexList.get(face.V1).Coord,  mVertexList.get(face.V2).Coord, Ires);

			if (nCollide == 1)
			{
				MatrixUtils.minus(Ires, R0, dir);
				float fDistanceToR0 = MatrixUtils.magnitude(dir);
				if (fDistanceToR0 <= fSmallestDistanceToR0)
				{
					MatrixUtils.copy(Ires, intersectPtReturn);
					nRes = i;
					fSmallestDistanceToR0 = fDistanceToR0;
				}
			}
		}
		return nRes;
	}
	
	// recycled vectors for time critical function where new are too long
	static float[] dir = new float[3];
	static float[] n = new float[3];
	static float SMALL_NUM = 0.00000001f; // anything that avoids division overflow
	static float[] u = new float[3];
	static float[] v = new float[3];
	static float[] w = new float[3];
	static float[] w0 = new float[3];
	static float[] zero = { 0, 0, 0 };

	// intersect_RayTriangle(): intersect a ray with a 3D triangle
	// Input: a ray R (R0 and R1), and a triangle T (V0,V1)
	// Output: *I = intersection point (when it exists)
	// Return: -1 = triangle is degenerate (a segment or point)
	// 0 = disjoint (no intersect)
	// 1 = intersect in unique point I1
	// 2 = are in the same plane
	static int intersect_RayTriangle(float[] R0, float[] R1, float[] V0, float[] V1, float[] V2, float[] Ires)
	{
		float r, a, b; // params to calc ray-plane intersect

		// get triangle edge vectors and plane normal
		MatrixUtils.minus(V1, V0, u);
		MatrixUtils.minus(V2, V0, v);

		MatrixUtils.cross(u, v, n); // cross product
		if (n == zero)
		{
			return -1; // do not deal with this case
		}

		MatrixUtils.minus(R1, R0, dir); // ray direction vector

		boolean bBackCullTriangle = MatrixUtils.dot(dir, n) > 0;// ray dir and normal have same direction
		if (bBackCullTriangle)
		{
			return 0;
		}

		MatrixUtils.minus(R0, V0, w0);
		a = -MatrixUtils.dot(n, w0);
		b = MatrixUtils.dot(n, dir);
		if (Math.abs(b) < SMALL_NUM)
		{ // ray is parallel to triangle plane
			if (a == 0)
			{
				return 2;
			} else
			{
				return 0; // ray disjoint from plane
			}
		}

		// get intersect point of ray with triangle plane
		r = a / b;
		if (r < 0.0)
		{
			return 0; // => no intersect
			// for a segment, also test if (r > 1.0) => no intersect
		}

		MatrixUtils.scalarMultiply(dir, r);
		MatrixUtils.plus(R0, dir, Ires);

		// is I inside T?
		float uu, uv, vv, wu, wv, D;
		uu = MatrixUtils.dot(u, u);
		uv = MatrixUtils.dot(u, v);
		vv = MatrixUtils.dot(v, v);
		MatrixUtils.minus(Ires, V0, w);
		wu = MatrixUtils.dot(w, u);
		wv = MatrixUtils.dot(w, v);
		D = uv * uv - uu * vv;

		// get and test parametric coords
		float s, t;
		s = (uv * wv - vv * wu) / D;
		if (s < 0.0 || s > 1.0)
		{
			return 0;
		}
		t = (uv * wu - uu * wv) / D;
		if (t < 0.0 || s + t > 1.0)
		{
			return 0;
		}

		return 1; // I is in T
	}
	
	public void InitGrabAction(int nTriangleIndex)
	{

	}
	

	// TODO place as an action
	public void ColorizePaintAction(int triangleIndex)
	{
		if (triangleIndex >= 0)
		{
			int color = getManagers().getToolsManager().getColor();
			Face face=mFaceList.get(triangleIndex);
			Vertex vertex=mVertexList.get(face.V0);//arbitrarily chosen point in triangle
			vertex.Color=color;
			
			UpdateVertexColor(face.V0);

			// First corona
			if (getManagers().getToolsManager().getRadius() >= 50)
			{				

			}
		}
	}
	

	// TODO place as an action
	public void RiseSculptAction(int triangleIndex)
	{
		if (triangleIndex >= 0)
		{
			float[] VOffset = new float[3];
			
			Face face=mFaceList.get(triangleIndex);
			Vertex vertex=mVertexList.get(face.V0);// first triangle point arbitrarily chosen (should take closest or retessalate)

			float fMaxDeformation = getManagers().getToolsManager().getStrength() / 100.0f * 0.2f;// strength is -100 to 100

			MatrixUtils.copy(vertex.Normal, VOffset);
			MatrixUtils.scalarMultiply(VOffset, fMaxDeformation);
			MatrixUtils.plus(vertex.Coord, VOffset, vertex.Coord);
			
			UpdateVertexValue(face.V0);

			// First corona
			if (getManagers().getToolsManager().getRadius() >= 50)
			{				
				// update normals after rise up				
			}

			ComputeVertexNormal(vertex);
		}
	}

	void Reset()
	{
		mVertexList.clear();
		mFaceList.clear();
		mEdgeList.clear();
	}

	void SubdivideAllFaces()
	{
		ArrayList<Face> mOrigFaceList = mFaceList;
		mFaceList=new ArrayList<Face>();
		int n = mOrigFaceList.size();
		for (int i = 0; i < n; i++)
		{
			Face face = mOrigFaceList.get(i);

			int nA=face.V0;
			int nB=face.V1;
			int nC=face.V2;
			
			Vertex A = mVertexList.get(nA);
			Vertex B = mVertexList.get(nB);
			Vertex C = mVertexList.get(nC);
			
			Vertex v0 = new Vertex(A, B);// takes mid point
			Vertex v1 = new Vertex(B, C);
			Vertex v2 = new Vertex(C, A);
			
			int nBase=mVertexList.size();
			int n0=nBase+0;
			int n1=nBase+1;
			int n2=nBase+2;
			
			mVertexList.add(v0);
			mVertexList.add(v1);
			mVertexList.add(v2);

			Face f0 = new Face(nA, n0, n2);
			Face f1 = new Face(n0, nB, n1);
			Face f2 = new Face(n1, nC, n2);
			Face f3 = new Face(n0, n1, n2);

			mFaceList.add(f0);
			mFaceList.add(f1);
			mFaceList.add(f2);
			mFaceList.add(f3);			
		}
	}
	
	public void UpdateVertexValue(int nVertexIndex)
	{
		Vertex vertex=mVertexList.get(nVertexIndex);
		for (RenderFaceGroup renderGroup : mRenderGroupList)
		{
			renderGroup.UpdateVertexValue( nVertexIndex, vertex.Coord, vertex.Normal);
		}
		UpdateBoudingSphereRadius(vertex.Coord);				
	}
	
	public void UpdateVertexColor( int nVertexIndex)
	{
		Vertex vertex=mVertexList.get(nVertexIndex);
		for (RenderFaceGroup renderGroup : mRenderGroupList)
		{
			renderGroup.UpdateVertexColor( nVertexIndex, vertex.Color);
		}		
	}

	void UpdateBoudingSphereRadius(float[] val)
	{
		float norm = MatrixUtils.magnitude(val);
		if (norm > mBoundingSphereRadius)
		{
			mBoundingSphereRadius = norm;
			getManagers().getPointOfViewManager().setRmin(1 + mBoundingSphereRadius);// takes near clip into accoutn, TODO read from conf
		} 
	}
}
