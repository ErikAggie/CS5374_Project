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

    /**
     * Equals override. Two LockInfos are equal if they have the same name and come from the same class.
     * @param o
     * @return
     */
    public boolean equals(Object o)
    {
        if ( !(o instanceof LockInfo))
        {
            return false;
        }

        LockInfo other = (LockInfo) o;

        return (other.name.equals(name) &&
                other.containingClass.equals(this.containingClass));
    }
}
