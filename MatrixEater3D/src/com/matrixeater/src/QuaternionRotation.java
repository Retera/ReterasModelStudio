package com.matrixeater.src;
import javax.swing.JOptionPane;
/**
 * Quaternions are the most useless thing I've ever heard of. Nevertheless, I wanted a simple
 * object to encompass four quaternion values for rotation (this is how MDLs handle rotating)
 * 
 * Eric Theller
 * 3/8/2012
 */
public class QuaternionRotation
{
    double a, b, c, d;
    public QuaternionRotation( double a, double b, double c, double d )
    {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }
    public QuaternionRotation(Vertex eulerRotation)
    {
//         eulerRotation.x = Math.toRadians(eulerRotation.x);
//         eulerRotation.y = Math.toRadians(eulerRotation.y);
//         eulerRotation.z = Math.toRadians(eulerRotation.z);
        //Original Wikipedia equation test
        
//         double yaw = eulerRotation.z;
//         yaw = Math.PI - yaw;
//         if( yaw > Math.PI )
//         {
//             yaw -= Math.PI;
//         }
//         eulerRotation.z = yaw;
//         eulerRotation.y = -eulerRotation.y;
        a = Math.cos(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)
         + Math.sin(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2);
        b = Math.sin(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)
         - Math.cos(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2);
        c = Math.cos(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2)
         + Math.sin(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2);
        d = Math.cos(eulerRotation.x / 2) * Math.cos(eulerRotation.y / 2) * Math.sin(eulerRotation.z / 2)
         - Math.sin(eulerRotation.x / 2) * Math.sin(eulerRotation.y / 2) * Math.cos(eulerRotation.z / 2);
         
         
        if( Math.abs(a) < 1E-15 )
        {
            a = 0;
        }
        if( Math.abs(b) < 1E-15 )
        {
            b = 0;
        }
        if( Math.abs(c) < 1E-15 )
        {
            c = 0;
        }
        if( Math.abs(d) < 1E-15 )
        {
            d = 0;
        }
//          b = -b;
//          c = -c;
         //new test
//          double c1 = Math.cos(eulerRotation.x / 2);
//          double c2 = Math.cos(eulerRotation.y / 2);
//          double c3 = Math.cos(eulerRotation.z / 2);
//          double s1 = Math.sin(eulerRotation.x / 2);
//          double s2 = Math.sin(eulerRotation.y / 2);
//          double s3 = Math.sin(eulerRotation.z / 2);
//          
//          a = c1 * c2 * c3 - s1 * s2 * s3;
//          b = s1 * s2 * c3 + c1 * c2 * s3;
//          c = s1 * c2 * c3 + c1 * s2 * s3;
//          d = c1 * s2 * c3 - s1 * c2 * s3;
         
         
        /**double heading = eulerRotation.x;
        double attitude = eulerRotation.y;
        double bank = eulerRotation.z;
        double c1 = Math.cos(heading);
        double s1 = Math.sin(heading);
        double c2 = Math.cos(attitude);
        double s2 = Math.sin(attitude);
        double c3 = Math.cos(bank);
        double s3 = Math.sin(bank);
        a = Math.sqrt(1.0 + c1 * c2 + c1*c3 - s1 * s2 * s3 + c2*c3) / 2.0;
        double w4 = (4.0 * a);
        b = (c2 * s3 + c1 * s3 + s1 * s2 * c3) / w4 ;
        c = (s1 * c2 + s1 * c3 + c1 * s2 * s3) / w4 ;
        d = (-s1 * s3 + c1 * s2 * c3 +s2) / w4 ;*/
         
         
        //Now Quaternions can go burn and die.
    }
    public double getA()
    {
        return a;
    }
    public double getB()
    {
        return b;
    }
    public double getC()
    {
        return c;
    }
    public double getD()
    {
        return d;
    }
    public Vertex toEuler()
    {
        //Wikipedia formula
        double roll = (Math.atan2( 2.0 * ( a * b + c * d ), 1 - 2.0 * ( b * b + c * c ) ) );
        double stuff =  a * c - d * b ;
        if( stuff > 0.5 )
        {
            stuff = 0.5;
        }
        else if( stuff < -0.5 )
        {
            stuff = -0.5;
        }
        double pitch = (Math.asin( 2.0 * (stuff) ) );
        double yaw = (Math.atan2( 2.0 * ( a * d + b * c ), 1 - 2.0 * ( c * c + d * d) ) );
        
//         yaw = Math.PI - yaw;
//         if( yaw > Math.PI )
//         {
//             yaw -= Math.PI;
//         }
//         pitch = -pitch;
        
//         //Wikipedia formula with some twists
//         double roll = (Math.atan2( 2.0 * ( d * a + b * c ), 1 - 2.0 * ( a * a + b * b ) ) );
//         double pitch = (Math.asin( 2.0 * ( d * b - c * a ) ) );
//         double yaw = (Math.atan2( 2.0 * ( d * c + a * b ), 1 - 2.0 * ( b * b + c * c) ) );
        
//         //www.eulideanspace.com formula
//         double bank = (Math.atan2( 2.0 * ( a * b - c * d ), 1 - 2.0 * ( b * b + d * d ) ) );
//         double attitude = (Math.asin( 2.0 * ( b * c + d * a ) ) );
//         double heading = (Math.atan2( 2.0 * ( c * d + b * d ), 1 - 2.0 * ( c * c + d * d) ) );
//         //Math.toDegrees
//         
//         if( b * c + d * a == 0.5 )
//         {
//             heading = 2 * Math.atan2(b,a);
//             bank = 0;
//         }
//         else if( b * c + d * a == -0.5 )
//         {
//             heading = - 2 * Math.atan2(b,a);
//             bank = 0;
//         }


        if( b * c + d * a == 0.5 )
        {
            roll = 2 * Math.atan2(b,a);
            pitch = 0;
        }
        else if( b * c + d * a == -0.5 )
        {
            roll = - 2 * Math.atan2(b,a);
            pitch = 0;
        }
         
        return new Vertex(roll, pitch, yaw);
//         return new Vertex(heading, attitude, bank);
//         Now Quaternions can go burn and die.
    }
    public static QuaternionRotation parseText(String input)
    {
        String [] entries = input.split(",");
        QuaternionRotation temp = null;
        double a = 0;
        double b = 0;
        double c = 0;
        double d = 0;
        String [] str = entries[0].split("\\{");
        try
        {
            a = Double.parseDouble(str[1]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: QuaternionRotation coordinates could not be interpreted.");
        }
        try
        {
            b = Double.parseDouble(entries[1]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: QuaternionRotation coordinates could not be interpreted.");
        }
        try
        {
            c = Double.parseDouble(entries[2]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: QuaternionRotation coordinates could not be interpreted.");
        }
        try
        {
            d = Double.parseDouble(entries[3].split("}")[0]);
        }
        catch( NumberFormatException e )
        {
            JOptionPane.showMessageDialog(MainFrame.panel,"Error {"+input+"}: QuaternionRotation coordinates could not be interpreted.");
        }
        temp = new QuaternionRotation(a,b,c,d);
        return temp;
    }
    public String toString()
    {
        return "{ "+MDLReader.doubleToString(a)+", "+MDLReader.doubleToString(b)+", "+MDLReader.doubleToString(c)+", "+MDLReader.doubleToString(d)+" }";
    }
    
    public static void main(String [] args)
    {
        QuaternionRotation rot = new QuaternionRotation( 0.241689, 0.152046, -0.372562, 0.882987 );
        Vertex euler = rot.toEuler();
        euler.x = -euler.x;
        Vertex eulerRotation = new Vertex(euler);
        
        eulerRotation.x = Math.toDegrees(eulerRotation.x);
        eulerRotation.y = Math.toDegrees(eulerRotation.y);
        eulerRotation.z = Math.toDegrees(eulerRotation.z);
        System.out.println(rot);
        System.out.println(eulerRotation);
        System.out.println(new QuaternionRotation(euler));
        
        System.out.println();
        rot = new QuaternionRotation( 0.241689, 0.152046, -0.372562, 0.882987 );
        euler = rot.toEuler();
        euler.y = -euler.y;
        eulerRotation = new Vertex(euler);
        
        eulerRotation.x = Math.toDegrees(eulerRotation.x);
        eulerRotation.y = Math.toDegrees(eulerRotation.y);
        eulerRotation.z = Math.toDegrees(eulerRotation.z);
        System.out.println(rot);
        System.out.println(eulerRotation);
        System.out.println(new QuaternionRotation(euler));
        
        System.out.println();
        rot = new QuaternionRotation( 0.241689, 0.152046, -0.372562, 0.882987 );
        euler = rot.toEuler();
        euler.z = -euler.z;
        eulerRotation = new Vertex(euler);
        
        eulerRotation.x = Math.toDegrees(eulerRotation.x);
        eulerRotation.y = Math.toDegrees(eulerRotation.y);
        eulerRotation.z = Math.toDegrees(eulerRotation.z);
        System.out.println(rot);
        System.out.println(eulerRotation);
        System.out.println(new QuaternionRotation(euler));
        
        
        System.out.println();
        euler = new Vertex(Math.PI*(20.0/90.0),0,0);
        euler.x = -euler.x;
        eulerRotation = new Vertex(euler);
        
        eulerRotation.x = Math.toDegrees(eulerRotation.x);
        eulerRotation.y = Math.toDegrees(eulerRotation.y);
        eulerRotation.z = Math.toDegrees(eulerRotation.z);
        System.out.println(eulerRotation);
        System.out.println(new QuaternionRotation(euler));
        System.out.println();
        System.out.println(new QuaternionRotation( 0, 0, 0.707107, 0.707107 ).toEuler());
        System.out.println(new QuaternionRotation( 0.707107, 0, 0, 0.707107 ).toEuler());
        System.out.println(new QuaternionRotation( 4.329780281177466E-017, 0.707107, 4.329780281177466E-017, 0.707107 ).toEuler());
    }
}
