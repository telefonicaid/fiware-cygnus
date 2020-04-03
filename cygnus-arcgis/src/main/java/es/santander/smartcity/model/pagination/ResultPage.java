/**
 * 
 */
package es.santander.smartcity.model.pagination;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author dmartinez
 *
 */
public class ResultPage<T> implements Iterable<T>, Serializable {
	
	
	private static final long serialVersionUID = 1L;

    private List<T> items = new ArrayList<T>();

    private int count = -1;

    private int pageSize;

    private int startIndex;

    private boolean hasNext;

    
    private static final ResultPage<?> EMPTY = new ResultPage<Object>(0, 0, Collections.emptyList());


    @SuppressWarnings("unchecked")
    public static <T> ResultPage<T> emptyPage()
    {

    	return (ResultPage<T>) EMPTY;
    }

    public ResultPage(int startIndex, int pageSize, List<T> list)
    {
	
		assert (list != null) : "list is null";
		assert (pageSize >= 0) : "pageSize <0";
		assert (startIndex >= 0) : "startIndex <0";
		this.items = list;
		this.pageSize = pageSize;
		this.startIndex = startIndex;
    }

    public ResultPage(int startIndex, int pageSize, int count, List<T> list)
    {

		this(startIndex, pageSize, list);
		assert (count >= 0) : "count<0";
		this.count = count;
    }


    public ResultPage(int startIndex, int pageSize, boolean hasNext, List<T> list)
    {

	this(startIndex, pageSize, list);
	this.hasNext = hasNext;
    }


    public List<T> asItemsList()
    {

	return items;
    }


    public boolean isEmpty()
    {

	return (items.size() != 0 ? false : true);
    }


    public List<T> getItems()
    {

	return Collections.unmodifiableList(items);
    }


    
    public T getItem(int numberElements, Boolean isUnique)
    {

		if(isUnique)
		{
	
		    if(items.size() == 0)
		    {
	
			throw new NoSuchElementException("No existen items.");
		    }
		    else if(items.size() > 1)
		    {
	
			throw new NoSuchElementException("Existe mÃ¡s de un item.");
		    }
		    else
		    {
	
			return items.get(numberElements);
		    }
	
		}
		else
		{
	
		    if(items.size() > 0)
		    {
			return items.get(numberElements);
		    }
		    else
		    {
			throw new NoSuchElementException("No existen items");
		    }
		}

    }


    
    @Deprecated
    public T getFirstItem()
    {

	if(items.size() > 0)
	{
	    return items.get(0);
	}
	else
	{
	    throw new NoSuchElementException("No existen items");
	}

    }


    
    public int getItemsSize()
    {

    	return items.size();
    }


    
    public boolean isCountAvailable()
    {

    	return count >= 0;
    }


    public int getCount()
    {

    	return count;
    }


    public int getPageSize()
    {

    	return (items.size() > pageSize) ? items.size() : pageSize;
    }


    public int getStartIndex()
    {

    	return startIndex;
    }


    public Iterator<T> iterator()
    {

    	return items.iterator();
    }


    
    public boolean hasNext()
    {

    	return !isLastPage() || (count < 0 && hasNext);
    }


    
    public boolean isLastPage()
    {

    	return !(getLastElementNumber() < getCount());
    }


    
    public boolean isMiddlePage()
    {

    	return (getCount() > 0 && !isLastPage() && !isFirstPage());
    }


    
    public boolean isUniquePage()
    {

    	return getNumberOfPages() <= 1;
    }


    
    public int getPageLastIndex()
    {
	
		if(items.isEmpty())
		{
		    return getStartIndex();
		}
			return getStartIndex() + items.size() - 1;
    }


    
    public int getNumberOfPages()
    {

		if(getPageSize() > 0)
		{
		    return (int) ((this.getCount() + this.getPageSize() - 1) / this.getPageSize());
		}
		return 0;
    }


    
    public int getFirstElementNumber()
    {

		if(items.isEmpty())
		{
		    return getStartIndex();
		}
		return getStartIndex() + 1;
    }

    public int getLastElementNumber()
    {

    	return getStartIndex() + items.size();
    }

    public int getPageNumber()
    {

		int pageSize = getPageSize();
		if(pageSize == 0)
		{
		    return 0;
		}
		return (int) ((getStartIndex() / pageSize) + 1);
    }


    public boolean isPreviousPageAvailable()
    {

    	return !isFirstPage();
    }

    public boolean isFirstPage()
    {

    	return (getStartIndex() == 0);
    }


    public int getNextPageNumber()
    {

    	return getPageNumber() + 1;
    }


    public int getPreviousPageNumber()
    {

    	return getPageNumber() > 0 ? getPageNumber() - 1 : 0;
    }


    public int getNextPageIndex()
    {

    	return getStartIndex() + getPageSize();
    }


    public int getPreviousPageIndex()
    {

		int p = getStartIndex() - getPageSize();
		return (p < 0) ? 0 : p;
    }


    public String toString()
    {

		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(" count:[").append(count).append("] hasNext:[").append(hasNext).append("] pageSize:[").append(pageSize).append("] startIndex:[").append(startIndex).append("] listSize:[").append(items.size()).append("] pageNumber:[").append(getPageNumber()).append("]");
		return sb.toString();

    }

}
