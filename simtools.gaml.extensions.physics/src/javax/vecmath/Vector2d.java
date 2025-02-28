/*******************************************************************************************************
 *
 * Vector2d.java, in simtools.gaml.extensions.physics, is part of the source code of the
 * GAMA modeling and simulation platform (v.1.9.3).
 *
 * (c) 2007-2023 UMI 209 UMMISCO IRD/SU & Partners (IRIT, MIAT, TLU, CTU)
 *
 * Visit https://github.com/gama-platform/gama for license information and contacts.
 * 
 ********************************************************************************************************/

package javax.vecmath;


/**
 * A 2-element vector that is represented by double-precision floating
 * point x,y coordinates.
 *
 */
public class Vector2d extends Tuple2d implements java.io.Serializable {

    /** The Constant serialVersionUID. */
    // Combatible with 1.1
    static final long serialVersionUID = 8572646365302599857L;

    /**
     * Constructs and initializes a Vector2d from the specified xy coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Vector2d(double x, double y)
    {
      super(x,y);
    }


    /**
     * Constructs and initializes a Vector2d from the specified array.
     * @param v the array of length 2 containing xy in order
     */
    public Vector2d(double[] v)
    {
      super(v);
    }


    /**
     * Constructs and initializes a Vector2d from the specified Vector2d.
     * @param v1 the Vector2d containing the initialization x y data
     */
    public Vector2d(Vector2d v1)
    {
       super(v1);
    }


    /**
     * Constructs and initializes a Vector2d from the specified Vector2f.
     * @param v1 the Vector2f containing the initialization x y data
     */
    public Vector2d(Vector2f v1)
    {
       super(v1);
    }


    /**
     * Constructs and initializes a Vector2d from the specified Tuple2d.
     * @param t1 the Tuple2d containing the initialization x y data
     */
    public Vector2d(Tuple2d t1)
    {
       super(t1);
    }


    /**
     * Constructs and initializes a Vector2d from the specified Tuple2f.
     * @param t1 the Tuple2f containing the initialization x y data
     */
    public Vector2d(Tuple2f t1)
    {
       super(t1);
    }


    /**
     * Constructs and initializes a Vector2d to (0,0).
     */
    public Vector2d()
    {
        super();
    }


  /**
   * Computes the dot product of the this vector and vector v1.
   * @param v1 the other vector
   */
  public final double dot(Vector2d v1)
    {
      return (this.x*v1.x + this.y*v1.y);
    }


    /**
     * Returns the length of this vector.
     * @return the length of this vector
     */
    public final double length()
    {
        return Math.sqrt(this.x*this.x + this.y*this.y);
    }

    /**
     * Returns the squared length of this vector.
     * @return the squared length of this vector
     */
    public final double lengthSquared()
    {
        return (this.x*this.x + this.y*this.y);
    }

    /**
     * Sets the value of this vector to the normalization of vector v1.
     * @param v1 the un-normalized vector
     */
    public final void normalize(Vector2d v1)
    {
        double norm;

        norm = 1.0/Math.sqrt(v1.x*v1.x + v1.y*v1.y);
        this.x = v1.x*norm;
        this.y = v1.y*norm;
    }

    /**
     * Normalizes this vector in place.
     */
    public final void normalize()
    {
        double norm;

        norm = 1.0/Math.sqrt(this.x*this.x + this.y*this.y);
        this.x *= norm;
        this.y *= norm;
    }


  /**
    *   Returns the angle in radians between this vector and the vector
    *   parameter; the return value is constrained to the range [0,PI].
    *   @param v1    the other vector
    *   @return   the angle in radians in the range [0,PI]
    */
   public final double angle(Vector2d v1)
   {
      double vDot = this.dot(v1) / ( this.length()*v1.length() );
      if( vDot < -1.0) vDot = -1.0;
      if( vDot >  1.0) vDot =  1.0;
      return((Math.acos( vDot )));

   }


}
