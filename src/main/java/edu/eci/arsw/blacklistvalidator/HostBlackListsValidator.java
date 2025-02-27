/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import edu.eci.arsw.blacklistvalidator.BlackListSearchThread;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {
    private static final List<Integer> blackListOcurrences = new ArrayList<>();
    private static final AtomicInteger ocurrencesCount = new AtomicInteger(0);
    private static final int DEFAULT_THREADS = 5;
    private static final int BLACK_LIST_ALARM_COUNT = 5;
    public boolean checkHost(String host) {
        return checkHost(host, DEFAULT_THREADS);
    }

    public boolean checkHost(String host, int numThread) {
        blackListOcurrences.clear();
        ocurrencesCount.set(0);

        HostBlackListsDataSourceFacade datasource = HostBlackListsDataSourceFacade.getInstance();
        int totalLists = datasource.getRegisteredServersCount();

        Thread[] threads = new Thread[numThread];
        int segmentSize = totalLists / numThread;

        for (int i = 0; i < numThread; i++) {
            int startIndex = i * segmentSize;
            int endIndex = (i == numThread - 1) ? totalLists : (i + 1) * segmentSize;

            BlackListSearchThread thread = new BlackListSearchThread(host, startIndex, endIndex);
            threads[i] = new Thread(thread);
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean isNotTrustworthy = ocurrencesCount.get() >= BLACK_LIST_ALARM_COUNT;

        if (isNotTrustworthy) {
            System.out.println("Host " + host + ocurrencesCount.get());
            for (Integer listIndex : blackListOcurrences) {
                System.out.println(listIndex);
            }
            datasource.reportAsNotTrustworthy(host);
        } else {
            System.out.println("Host " + host + ocurrencesCount.get());
            datasource.reportAsTrustworthy(host);
        }

        return isNotTrustworthy;
    }
}

