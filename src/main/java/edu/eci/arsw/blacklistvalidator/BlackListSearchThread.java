package edu.eci.arsw.blacklistvalidator;

import static edu.eci.arsw.blacklistvalidator.HostBlackListsValidator.*;
import edu.eci.arsw.spamkeywordsdatasource.*;

public class BlackListSearchThread implements Runnable {
    private final String host;
    private final int startIndex;
    private final int endIndex;
    private final HostBlackListsDataSourceFacade datasource;

    public BlackListSearchThread(String host, int startIndex, int endIndex) {
        this.host = host;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.datasource = HostBlackListsDataSourceFacade.getInstance();
    }

    @Override
    public void run() {
        for (int i = startIndex; i < endIndex && ocurrencesCount.get() < BLACK_LIST_ALARM_COUNT; i++) {
            if (datasource.isInBlackListServer(i, host)) {
                synchronized (blackListOcurrences) {
                    blackListOcurrences.add(i);
                }
                ocurrencesCount.incrementAndGet();
            }
        }
    }
}