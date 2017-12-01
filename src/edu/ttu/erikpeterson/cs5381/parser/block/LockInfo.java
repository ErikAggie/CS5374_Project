package edu.ttu.erikpeterson.cs5381.parser.block;

/**
 * Describes a lock made by a thread
 */
public class LockInfo {
    private final String name;
    private final String type;


    private final String whereFound;
    private final boolean lock;

    public LockInfo(String name, String containingClass, String whereFound, boolean lock)
    {
        this.name = name;
        this.type = containingClass;
        this.whereFound = whereFound;
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
