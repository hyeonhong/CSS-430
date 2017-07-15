/**
 *
 * @author      Hyeon Hong
 * @date        08/18/2016
 * @assignment  Lab 4
 *
 *
 * Cache class implements a buffer cache that stores frequently accessed disk blocks in memory.
 * The buffer cache uses the enhanced second-chance algorithm to determine a victim page.
 *
 *
 */


import java.util.*;

public class Cache {
	private Entry[] pageTable;			// page table in memory
	private int blockSize;				// size of each data in a page
	private int victimIndex;			// keeps track of index for victim page in the pageTable


	/**
	 * The constructor initializes the pageTable with Entry objects and set the victimIndex to the last page of pageTable.
	 * The blockSize is set to the passed-in parameter blockSizeParm.
	 * 
	 * @param blockSizeParam The size of each data
	 * @param pageTableLength The total number of entries in pageTable
	 */
    public Cache(int blockSizeParam, int pageTableLength) {
    	
    	pageTable = new Entry[pageTableLength];
    	for (int i = 0; i < pageTableLength; i++) {
    		pageTable[i] = new Entry();
    	}
    	
    	blockSize = blockSizeParam;
    	victimIndex = pageTableLength - 1;		// set to the last page in the pageTable
    }


    /**
     * Each Entry object corresponds to each page in the pageTable.
     */
    private class Entry {
    	byte[] data;
    	int blockId;
    	boolean referenceBit;
    	boolean dirtyBit;
    	
    	private Entry() {
    		data = new byte[blockSize];
    		blockId = -1;
    		referenceBit = false;
    		dirtyBit = false;
    	}
    }

    /**
     * Finds a free page in the pageTable and returns its index. If not found, returns -1.
     * 
     * @return index for the free page
     */
    private int findFreePage() {
    	for (int i = 0; i < pageTable.length; i++) {
    		if (pageTable[i].blockId != -1) {
    			continue;
    		}
    		return i;
    	}  	
        return -1;
    }

    /**
     * Chooses the next victim page in the pageTable.
     * It increments the victimIndex and checks to see if the page had been recently used.
     * If the referenceBit is true, then flip the referenceBit and keep looping
     * until it reaches the condition in which the referenceBit is false.
     * 
     * @return index for victim page
     */
    private int nextVictim() {
    	while (true) {
    		victimIndex = (victimIndex + 1) % pageTable.length;
    		if (!pageTable[victimIndex].referenceBit) {
    			return victimIndex;
    		}
    		pageTable[victimIndex].referenceBit = false;
    	}
    }

    /**
     * Writes back all the pages that have been modified.
     * If the dirtyBit is true, write the data back to the disk and flip the dirtyBit.
     * 
     * @param victimEntry index for the page whose dirtyBit will be checked in this method
     */
    private void writeBack(int victimEntry) {
    	if (pageTable[victimEntry].dirtyBit) {
    		byte[] readFromCache = pageTable[victimEntry].data;
    		SysLib.rawwrite(pageTable[victimEntry].blockId, readFromCache);
    	
    		pageTable[victimEntry].dirtyBit = false;
    	}
    }
    
    /**
     * Copies the original array to the newArray and returns it.
     * 
     * @param original array that is going to be copied from
     * @return array that is going to be copied to
     */
    private byte[] copyArray(byte original[]) {
    	byte[] newArray = new byte[blockSize];
    	System.arraycopy(original, 0, newArray, 0, blockSize);
    	return newArray;
    }

    /**
     * Copies the original array into the passed-in buffer array.
     * 
     * @param original array that is going to be copied from
     * @param buffer array that is going to be copied to
     */
	private void readIntoBuffer(byte original[], byte buffer[]) {
    	System.arraycopy(original, 0, buffer, 0, blockSize);
    }

	/**
	 * Reads into the buffer array the cache block specified by blockId from the disk cache if it is in cache.
	 * Otherwise, reads the corresponding disk block from the disk device. 
	 * 
	 * @param blockId The number that specifies the block to read from
	 * @param buffer array in which the data is going to be stored
	 * @return Upon error, return false; otherwise return true.
	 */
    public synchronized boolean read(int blockId, byte buffer[]) {
    	
        if (blockId < 0) {
            SysLib.cerr("Error: Invalid blockId \n");
            return false;
            
        } else {
        
	        for (int i = 0; i < pageTable.length; i++) {
	            if (pageTable[i].blockId != blockId) {
	            	continue;
	            }
	            
	            // The specified block is in Cache.

	            // Reads the block's data into the buffer array
	            readIntoBuffer(pageTable[i].data, buffer);
	            
	            pageTable[i].referenceBit = true;
	            
	            return true;
	        }
	        
	        // The specified block is not in Cache.

	        // Search for the free page in the page table.
	        int index = findFreePage();
	        
	        // If there's no free page, choose the next victim page.
	        if (index == -1) {
	        	index = nextVictim();
	        	
	        	// If the data had been modified, then write it back to disk.
		        writeBack(index);
	        }
	        
	        // Fetch the data from the disk and store into the buffer array.
	        SysLib.rawread(blockId, buffer);
	        	        
	        // Write to the pageTable
	        pageTable[index].data = copyArray(buffer);
	        pageTable[index].blockId = blockId;
	        pageTable[index].referenceBit = true;
	
	        return true;
        }
    }

    /**
     * Writes the buffer array contents to the cache block specified by blockId from the disk cache if it is in cache.
     * Otherwise, finds a free cache block and writes the buffer contents on it. No write through.
     * 
     * @param blockId The number that specifies the block to write to
     * @param buffer array that contains the data for write operation
     * @return Upon error, return false; otherwise return true.
     */
    public synchronized boolean write(int blockId, byte buffer[]) {
    	
        if (blockId < 0) {
            SysLib.cerr("Error: Invalid blockId \n");
            return false;

        } else {
	        
	        for (int i = 0; i < pageTable.length; i++) {
	            if (pageTable[i].blockId != blockId) {
	            	continue;
	            }
	            
	            // The specified block is in Cache.
	            
	            // Write to the pageTable
	            pageTable[i].data = copyArray(buffer);
	            pageTable[i].referenceBit = true;
	            pageTable[i].dirtyBit = true;		// Mark the dirtyBit so that the data can be written back later
	            return true;
	        }
	        
	        // The specified block is not in Cache.

	        // Search for the free page in the page table.
	        int index = findFreePage();

	        // If there's no free page, choose the next victim page.
	        if (index == -1) {
	            index = nextVictim();
	            
		        // If the data had been modified, then write it back to disk.
		        writeBack(index);
	        }
	        	        
	        // Write to the pageTable
	        pageTable[index].data = copyArray(buffer);
	        pageTable[index].blockId = blockId;
	        pageTable[index].referenceBit = true;
	        pageTable[index].dirtyBit = true;		// Mark the dirtyBit so that the data can be written back later
	        
	        return true;
        }
    }

    /**
     * Writes back all dirty blocks to Disk.java and thereafter forces Disk.java
     * to write back all contents to the DISK file. It doesn't reset the contents in the pageTable.
     */
    public synchronized void sync() {
    	for (int i = 0; i < pageTable.length; i++) {
    		writeBack(i);
    	}
    	
    	SysLib.sync();
    }

    /**
     * Writes back all dirty blocks to Disk.java and thereafter forces Disk.java
     * to write back all contents to the DISK file. Resets all the entries in the pageTable.
     */
    public synchronized void flush() {
    	for (int i = 0; i < pageTable.length; i++) {
    		writeBack(i);
    		pageTable[i].blockId = -1;
    		pageTable[i].referenceBit = false;
    		pageTable[i].dirtyBit = false;
    	}
    	
    	SysLib.sync();
    }
}
