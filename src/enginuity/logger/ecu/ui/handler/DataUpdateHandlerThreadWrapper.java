/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package enginuity.logger.ecu.ui.handler;

import enginuity.logger.ecu.definition.LoggerData;
import static enginuity.util.ThreadUtil.sleep;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import java.util.List;

public final class DataUpdateHandlerThreadWrapper implements DataUpdateHandler, Runnable {
    private final List<DataUpdate> updateList = synchronizedList(new ArrayList<DataUpdate>());
    private final List<DataUpdate> workingList = synchronizedList(new ArrayList<DataUpdate>());
    private final DataUpdateHandler wrappee;
    private boolean stop = false;

    public DataUpdateHandlerThreadWrapper(DataUpdateHandler wrappee) {
        this.wrappee = wrappee;
    }

    public void registerData(LoggerData loggerData) {
        wrappee.registerData(loggerData);
    }

    public synchronized void handleDataUpdate(LoggerData loggerData, double value, long timestamp) {
        updateList.add(new DataUpdate(loggerData, value, timestamp));
    }

    public void deregisterData(LoggerData loggerData) {
        wrappee.deregisterData(loggerData);
    }

    public void cleanUp() {
        stop = true;
        wrappee.cleanUp();
    }

    public void reset() {
        wrappee.reset();
    }

    public void run() {
        while (!stop) {
            updateWorkingList();
            for (final DataUpdate dataUpdate : workingList) {
                wrappee.handleDataUpdate(dataUpdate.getLoggerData(), dataUpdate.getValue(), dataUpdate.getTimestamp());
            }
            sleep(3);
        }
    }

    private synchronized void updateWorkingList() {
        workingList.clear();
        for (DataUpdate dataUpdate : updateList) {
            workingList.add(dataUpdate);
        }
        updateList.clear();
    }

    private static final class DataUpdate {
        private final LoggerData loggerData;
        private final double value;
        private final long timestamp;

        public DataUpdate(LoggerData loggerData, double value, long timestamp) {
            this.loggerData = loggerData;
            this.value = value;
            this.timestamp = timestamp;
        }

        public LoggerData getLoggerData() {
            return loggerData;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public double getValue() {
            return value;
        }

    }
}