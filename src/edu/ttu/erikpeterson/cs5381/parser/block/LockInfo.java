package edu.ttu.erikpeterson.cs5381.parser.block;

/**
 * Describes a lock made by a thread
 */
public class LockInfo {
    private final String name;
    private final String containingClass;
    private final boolean lockUnlock;

    public LockInfo(String name, String containingClass, boolean lockUnlock)
    {
        this.name = name;
        this.containingClass = containingClass;
        this.lockUnlock = lockUnlock;
    }

    public String getName() {
        return name;
    }

    public String getContainingClass() {
        return containingClass;
    }

    public boolean isLock() {
        return lockUnlock;
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
                other.containingClass.equals(this.containingClass) &&
                other.lockUnlock == lockUnlock);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + containingClass.hashCode();
        result = 31 * result + (lockUnlock ? 1 : 0);
        return result;
    }
}
