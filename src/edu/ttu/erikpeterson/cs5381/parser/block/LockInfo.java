package edu.ttu.erikpeterson.cs5381.parser.block;

/**
 * Describes a lock made by a thread
 */
public class LockInfo {
    private final String name;
    private final String type;


    private final String whereFound;
    private final boolean lock;

    /**
     * Constructor
     *
     * @param name Name of this lock
     * @param type Type of this lock
     * @param method Where the lock was found
     * @param lock Lock or unlock?
     */
    public LockInfo(String name, String type, MethodBlock method, boolean lock)
    {
        this.name = name;
        this.type = type;
        this.whereFound = method.getClassAndName();
        this.lock = lock;
    }

    /**
     * Constructor based on another lock, but with the ability to change the lock behavior.
     * @param other LockInfo
     * @param lock Is this a lock or unlock?
     */
    public LockInfo(LockInfo other, boolean lock)
    {
        this.name = other.name;
        this.type = other.type;
        this.whereFound = other.whereFound;
        this.lock = lock;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getWhereFound() { return whereFound; }

    public boolean isLock() {
        return lock;
    }

    @Override
    public boolean equals(Object o)
    {
        if ( !(o instanceof LockInfo))
        {
            return false;
        }

        LockInfo other = (LockInfo) o;

        return (other.name.equals(name) &&
                other.type.equals(this.type) &&
                other.lock == lock);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (lock ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        String returnValue = "Variable " + name + " (" + type + ") found in " + whereFound;
        if ( !lock)
        {
            returnValue = returnValue + " (unlock)";
        }
        return returnValue;
    }
}
