// Aedan Gilbert D. Deyto
// Project 3 Problem 2 for COP4520
// 4/13/2022

import java.util.*;
import java.io.*;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

class Node
{
    // The Node class
    private int key;
    // Is used to tell the size of a list
    AtomicInteger size = new AtomicInteger(0);
    AtomicMarkableReference<Node> next = new AtomicMarkableReference<>(null, false);

    Node(int key)
    {
        this.key = key;
    }

    public int getKey()
    {
        return key;
    }
}

class ReadingList
{
    // An ArrayList of Heads of Linked List
    public List<Node> listHead = new ArrayList<Node>();

    class Window
    {
        public Node pred;
        public Node curr;

        Window(Node pred, Node curr)
        {
            this.pred = pred;
            this.curr = curr;
        }
    } // End of Window

    // Exact same function in the BirthdayPresentsParty program
    public Window find(Node head, int key)
    {
        Node pred = null;
        Node curr = null;
        Node succ = null;
        boolean [] marked = {false};
        boolean snip;

        retry : while (true)
        {
            pred = head;
            curr = pred.next.getReference();

            while (true)
            {
                succ = curr.next.get(marked);

                while (marked[0])
                {
                    snip = pred.next.compareAndSet(curr, succ, false, false);

                    if (snip == false) continue retry;

                    curr = succ;
                    succ = curr.next.get(marked);
                }

                if (curr.getKey() >= key)
                {
                    return new Window(pred, curr);
                }

                pred = curr;
                curr = succ;
            }
        }
    } // End of Find

    // Edited to allows duplicates and gave it an index parameter
    // So it knows which list to add in
    public boolean add (int key, int index)
    {
        while (true)
        {
            Window window = find(listHead.get(index), key);
            Node pred = window.pred;
            Node curr = window.curr;

            Node node = new Node(key);
            node.next = new AtomicMarkableReference<>(curr, false);

            if (pred.next.compareAndSet(curr, node, false, false))
            {
                // If a node is successfully added Increment the size of the list in the head node
                int increaseSize = listHead.get(index).size.getAndIncrement();
                return true;
            }
        }
    } // End of Add

    // traverse the list for the next largest value based on key
    public int traverse(int key)
    {
        Node temp = listHead.get(0).next.getReference();

        while (key >= temp.getKey())
        {
            // return a -2 to show we are at the end of the list
            if (temp.next.getReference() == null)
            {
                return -2;
            }

            temp = temp.next.getReference();
        }

        if (temp.getKey() == 171)
        {
            return -2;
        }

        return temp.getKey();
    }

    // Gets the max value of a speficied list indicated by index
    public int getMax(int index)
    {
        Node temp = listHead.get(index);
        int max = -2;

        // Keeps traversing the list until its reachs the tail
        while (temp.getKey() > max)
        {
            max = temp.getKey();
            temp = temp.next.getReference();

            if (temp.getKey() == 171 || temp.next.getReference() == null)
            {
                break;
            }
        }

        return max;
    }

    // Gets the min value of a speficied list indicated by index
    public int getMin(int index)
    {
        Node temp = listHead.get(index);
        // Just returns the key of the first node after head
        return temp.next.getReference().getKey();
    }

    // Returns the size of a specific list
    public int getSize(int index)
    {
        return listHead.get(index).size.get();
    }

    // Clears the ArrayList of head nodes
    public void clearList()
    {
        listHead.clear();
    }
}

class Sensors implements Runnable
{
    private TemperatureModule module;
    private CountDownLatch count;
    private ReadingList list;
    private int id;
    private int job = 0;
    private boolean canRun;
    private Random rand = new Random();

    Sensors(int id, TemperatureModule module, CountDownLatch count, ReadingList list)
    {
        // Sends the list all the need class its going to interact with
        this.id = id;
        this.module = module;
        this.count = count;
        this.list = list;
        canRun = true;

        // Tells the 9 thraed what job it will be performing
        if (id == 8)
        {
            job = 1;
        }
    }

    @Override
    public void run()
    {
        if (job == 0)
        {
            // For the 8 sensors they will perform 60 reading
            for (int i = 0; i < 60; i++)
            {
                // First get the reading
                int getReading = rand.nextInt(171);

                // Then add the reading to the main list and the minute list
                boolean addTemp = list.add(getReading, 0);
                addTemp = list.add(getReading, i + 1);

                int increaseSize = module.sizeOfList.getAndIncrement();
            }
        }

        else
        {
            // The list deals with doing the calulations
            // and i represents the minute we are looking into
            int i = 0;
            ArrayList<Integer> listMax = new ArrayList<>();
            ArrayList<Integer> listMin = new ArrayList<>();
            ArrayList<Integer> listDiff = new ArrayList<>();

            while(canRun)
            {
                // First checks if the Linked list for a ceratin minute is 8 to
                // show that the minute has passed
                if (list.getSize(i + 1) == 8)
                {
                    // Make sure the array list doesn't exceed size 10 for the 10
                    // minute intervel. If so remove the head of the list
                    if (listMax.size() == 10)
                    {
                        int remove = listMax.remove(0);
                        remove = listMin.remove(0);
                    }

                    // Add the max and min of the minute linked list into
                    // there respective ArrayList
                    boolean add = listMax.add(list.getMax(i + 1));
                    add = listMin.add(list.getMin(i + 1));

                    // Get the max and min value at the currnet momnet based
                    // on the max and min ArrayList
                    ArrayList<Integer> temp = new ArrayList<>(listMax);
                    int size = temp.size();
                    int max = temp.get(size - 1);

                    temp = new ArrayList<>(listMin);
                    int min = temp.get(0);

                    // And add the difference into the difference list
                    add = listDiff.add(max - min);

                    // Then Increment to the next minute
                    i++;
                }

                // If we have gone through all 60 minutes then get the results
                if (i == 60)
                {
                    // traverse the diff list to find where the max difference is
                    // and at what minute
                    int maxDif = 0;
                    int time = 0;
                    for (int j = 0; j < 60; j++)
                    {
                        if (maxDif < listDiff.get(j))
                        {
                            maxDif = listDiff.get(j);
                            time = j + 1;
                        }
                    }

                    // Then traverse the main list to get the top five min temp
                    ArrayList<Integer> topMin = new ArrayList<>();
                    int key = -1;

                    while (topMin.size() != 5)
                    {
                        key = list.traverse(key);
                        topMin.add(key - 100);
                    }

                    // Then traverse the main list to get the top five max temp
                    ArrayList<Integer> topMax = new ArrayList<>();
                    key = -1;

                    while (true)
                    {
                        key = list.traverse(key);
                        if (key == -2)
                        {
                            break;
                        }

                        // Make sure the ArrayList doesn't exceed 5
                        if (topMax.size() == 5)
                        {
                            int remove = topMax.remove(0);
                        }

                        topMax.add(key - 100);
                    }

                    // Print the results
                    System.out.println("The Top 5 Lowest Temp is " + topMin);
                    System.out.println("The Top 5 Highest Temp is " + topMax);
                    System.out.println("At time " + time + " has the hightest temp difference of " + (maxDif - 100) + " within a 10 minute intervel.");
                    canRun = false;
                }
            }

        }

        count.countDown();
    }
}

public class TemperatureModule
{
    AtomicInteger totalReadings = new AtomicInteger(480);
    AtomicInteger sizeOfList = new AtomicInteger(0);

    public static void main(String [] args)
    {
        TemperatureModule module = new TemperatureModule();
        ReadingList list = new ReadingList();
        long start = System.nanoTime();

        // represents how many hours we are going to perform the Temperature Reading Module
        for (int j = 0; j < 10; j++)
        {
            CountDownLatch count = new CountDownLatch(9);
            ExecutorService executor = Executors.newFixedThreadPool(9);

            // Fills the ArrayList in list Reading with the head of a list
            // Index o represents all reading
            // Index 1-60 represents the temp reading of the 8 sensor at the current minute
            for (int i = 0; i < 61; i++)
            {
                Node head = new Node(-1);
                Node tail = new Node(171);
                head.next = new AtomicMarkableReference<>(tail, false);

                list.listHead.add(head);
            }

            // Creates all 9 threads
            // 8 Thrads are the sensor the 9th thraed is the reading thread
            for (int i = 0; i < 9; i++)
            {
                executor.submit(new Sensors(i, module, count, list));
            }

            executor.shutdown();

            // Waits untill all threds are shutdown
            try
            {
                count.await();
            }
            catch (Exception error)
            {
                error.printStackTrace();
            }

            // Resent all values for the next hour
            list.clearList();
            module.sizeOfList = new AtomicInteger(0);

            System.out.println("==================== Hour: " + (j + 1) + " ====================");
        }

        long end = System.nanoTime();
        long exectution = end - start;
        double convert = exectution / 1000000;

        System.out.println("Execution Time: " + convert + " Milliseconds");
    }
}
