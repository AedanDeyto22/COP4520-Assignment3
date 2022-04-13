// Aedan Gilbert D. Deyto
// Project 3 Problem 1 for COP4520
// 4/13/2022

import java.util.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

class Node
{
    private int key;
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

class PresentsList
{
    public Node head;
    public Node tail;

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

    public Window find(Node head, int key)
    {
        Node pred = null;
        Node curr = null;
        Node succ = null;
        boolean [] marked = {false};
        boolean snip;

        // traverse the List to find where the key might end up at
        retry : while (true)
        {
            // Keep updaing the predicesor and current until we return
            pred = head;
            curr = pred.next.getReference();

            while (true)
            {
                // Get the reference and marked value of the node after current
                succ = curr.next.get(marked);

                // If is marked true then try removing current to clean up the thread
                while (marked[0])
                {
                    snip = pred.next.compareAndSet(curr, succ, false, false);

                    if (snip == false) continue retry;

                    curr = succ;
                    succ = curr.next.get(marked);
                }

                // When we reach our destination return a new Window class that
                // hold the addess of the node that should conatin key and its predicesor
                if (curr.getKey() >= key)
                {
                    return new Window(pred, curr);
                }

                pred = curr;
                curr = succ;
            }
        }
    } // End of Find

    public boolean add (int key)
    {
        while (true)
        {
            // First find where the key might be
            // And then get its predicesor and current node
            Window window = find(head, key);
            Node pred = window.pred;
            Node curr = window.curr;

            // Check to make sure the current node contains key if so
            // return false to show that key is already in the list
            if (curr.getKey() == key)
            {
                return false;
            }

            else
            {
                // Create a new node for key and sets it tail to the curret node
                Node node = new Node(key);
                node.next = new AtomicMarkableReference<>(curr, false);

                // Try adding the new node into the list until its successful
                if (pred.next.compareAndSet(curr, node, false, false))
                {
                    return true;
                }
            }
        }
    } // End of Add

    public boolean remove(int key)
    {
        boolean snip;

        while (true)
        {
            // First find where the key might be
            // And then get its predicesor and current node
            Window window = find(head, key);
            Node pred = window.pred;
            Node curr = window.curr;

            // Check to make sure the current node contains key if not
            // return false to show that key is not in the list
            if (curr.getKey() != key)
            {
                return false;
            }

            else
            {
                // Get the reference of the node after current.
                // And marked it to show that being manipulated
                Node succ = curr.next.getReference();
                snip = curr.next.compareAndSet(succ, succ, false, true);

                // If failed try again
                if (!snip)
                {
                    continue;
                }

                // If its able to mark it then perfrom a compare and set and
                // remove the current node
                pred.next.compareAndSet(curr, succ, false, false);
                return true;
            }
        }
    } // End of Remove

    public boolean contains(int key)
    {
        boolean [] marked = {false};
        Node curr = head;

        // We keep traversing until we get where the key might be located
        // At the same thim we also get the marked value of a node
        while (curr.getKey() < key)
        {
            curr = curr.next.getReference();
            Node succ = curr.next.get(marked);
        }

        // If the node is not marked for deletion and it has he key we are looking
        // for return false, otherwise return false
        if (curr.getKey() == key && marked[0] == false)
        {
            return true;
        }

        else
        {
            return false;
        }
    } // End of contains
}

class Servents implements Runnable
{
    private int name;
    private BirthdayPresentsParty minotour;
    private PresentsList presents;
    private boolean canRun;
    private Random rand = new Random();

    Servents(int name, BirthdayPresentsParty minotour, PresentsList presents)
    {
        this.name = name;
        this.minotour = minotour;
        this.presents = presents;
        canRun = true;
    }

    @Override
    public void run()
    {
        while(canRun)
        {
          // First the servent has to choose a job to perforn
          // It has a 2/5 chance each to either add a gift to the list or remove and create a thank you card
          // And a 1/5 chance to check for a gift in the list
          int chooseJob = rand.nextInt(5);

          if (chooseJob <= 1)
          {
              // Can only add a gift if we haven't gone above the amount still left
              if (minotour.getGiftIndex.get() < minotour.presentUnsorted.size())
              {
                  // Get a the gift index
                  int chooseGift = minotour.getGiftIndex.getAndIncrement();

                  // Check to make sure we haven't gone over the index
                  if (chooseGift < 500000)
                  {
                      // Get the gift from the unsorted list and add it into the sorted linked list
                      int getGift = minotour.presentUnsorted.get(chooseGift);
                      boolean didItWork = presents.add(getGift);

                      // Increment the size of list so we know we added all 500,000 gifts
                      int increase = minotour.sizeOfList.getAndIncrement();
                  }
              }
          } // End of Choice 1

          else if (chooseJob > 1 && chooseJob <= 3)
          {
              // Remove a present from the List only if there is a gift in the list
              if (minotour.sizeOfList.get() > 0 && minotour.removeGiftIndex.get() < minotour.sizeOfList.get())
              {
                  // We get the index of the gift we are removing
                  int chooseGift = minotour.removeGiftIndex.getAndIncrement();

                  if (chooseGift < 500000)
                  {
                      // We then get the gift ID from the unsortedd list using the index we got
                      int getGift = minotour.presentUnsorted.get(chooseGift);

                      // We first try to remove the gift
                      boolean didItWork = presents.remove(getGift);

                      // We might fail to remove it since its in the process of  getting added
                      // So we keep trying untils it able to remove it
                      while (presents.contains(getGift) == true || didItWork == false)
                      {
                          didItWork = presents.remove(getGift);
                      }

                      // This represents that we remove the gift from the list and made a thank you card
                      int decrement = minotour.thankYouCard.getAndIncrement();
                  }
              }
          }

          else
          {
              // We get the minotours request by randomly getting a number
              int minotourRequest = rand.nextInt(500000);

              // And then checking if the present is within the list
              boolean checkList = presents.contains(minotourRequest);
          }

          // The thread only stops when all 500000 gift are all sorted and have gotten a thank you card
          if (minotour.sizeOfList.get() == 500000 && minotour.thankYouCard.get() == 500000)
          {
              canRun = false;
          }
        } // End of While Loop
    } // End of run
}

public class BirthdayPresentsParty
{
    public ArrayList<Integer> presentUnsorted = new ArrayList<>();
    public AtomicInteger sizeOfList = new AtomicInteger(0);
    public AtomicInteger thankYouCard = new AtomicInteger(0);
    public AtomicInteger getGiftIndex = new AtomicInteger(0);
    public AtomicInteger removeGiftIndex = new AtomicInteger(0);
    public AtomicInteger presentsAmount = new AtomicInteger(500000);

    public static void main(String [] args)
    {
        BirthdayPresentsParty minotour = new BirthdayPresentsParty();
        PresentsList list = new PresentsList();

        // Here we create an ArrayList that will act as our unsorted gift list
        for (int i = 0; i < 500000; i++)
        {
            minotour.presentUnsorted.add(i);
        }

        // We need to shuffle the list so that we can get an unsorted list;
        Collections.shuffle(minotour.presentUnsorted);

        // Set up the Linked List
        list.head = new Node(-1);
        list.tail = new Node(500001);
        list.head.next = new AtomicMarkableReference<>(list.tail, false);

        // Starts the timer for the threads
        long start = System.nanoTime();

        // Create 4 threads
        Servents servent1 = new Servents(1, minotour, list);
        Servents servent2 = new Servents(2, minotour, list);
        Servents servent3 = new Servents(3, minotour, list);
        Servents servent4 = new Servents(4, minotour, list);

        Thread thread1 = new Thread(servent1);
        Thread thread2 = new Thread(servent2);
        Thread thread3 = new Thread(servent3);
        Thread thread4 = new Thread(servent4);

        // Starts running the 4 Threads
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        // Waits until all Threads are done
        try
        {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        }
        catch (Exception error)
        {
            error.printStackTrace();
        }

        long end = System.nanoTime();
        long exectution = end - start;
        double convert = exectution / 1000000;

        System.out.println("Execution Time: " + convert + " Milliseconds");
        System.out.println("The amount of Gifts add to the list: " + minotour.sizeOfList.get());
        System.out.println("The amount of Thank You cards made: " + minotour.thankYouCard.get());
    }
}
