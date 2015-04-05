import json, os
from common.context import Service
from common import utils
import logging, traceback
from threading import RLock
from threading import Thread
from common.utils import log_execution


class ThreadManager(Service):

    def __init__(self):
        super(ThreadManager, self).__init__(ThreadManager)
        self._threadMap = {}
        self._threadMapLock = RLock()

    def post(self, thread_id, runnable):
        assert isinstance(runnable, ThreadRunnable)
        self._threadMapLock.acquire()
        runnable_list = self._threadMap.get(thread_id, None)
        thread_start_required = False
        if runnable_list is None:
            runnable_list = []
            self._threadMap[thread_id] = runnable_list
        if len(runnable_list) == 0:
            thread_start_required = True

        log_execution().info("Append new task for tread "+thread_id)
        runnable_list.append(runnable)
        if thread_start_required:

            thread = _ExecutionThread(thread_id,self)
            log_execution().info("Starting tread "+thread_id)
            thread.start()
        pass
        self._threadMapLock.release()

    def _provide_runnable(self, thread_id):
        try:
            self._threadMapLock.acquire()
            runnable_list = self._threadMap.get(thread_id, None)
            if runnable_list is None:
                return None
            if len(runnable_list) == 0:
                return None
            return runnable_list[0]
        finally:
            self._threadMapLock.release()

    def _remove_runnable(self, thread_id, runnable):
        try:
            self._threadMapLock.acquire()
            runnable_list = self._threadMap.get(thread_id, None)
            if runnable_list is None:
                return None
            if len(runnable_list) == 0:
                return None
            runnable_list.remove(runnable)
        finally:
            self._threadMapLock.release()
        pass

    def _on_thread_shutdown(self, thread_id):
        self._threadMapLock.acquire()
        log_execution().info("Stopping tread "+thread_id)
        self._threadMapLock.release()



class ThreadRunnable(object):

    def __init__(self):
        super(ThreadRunnable, self).__init__()

    def run(self):
        pass


class _ExecutionThread(Thread):

    def __init__(self, thread_id, thread_manager):
        super(_ExecutionThread, self).__init__()
        self._thread_prog_name = thread_id
        self.setName(thread_id+"_thread")
        assert isinstance(thread_manager, ThreadManager)
        self.tm = thread_manager

    def run(self):
        runnable = self.tm._provide_runnable(self._thread_prog_name)
        assert isinstance(runnable, ThreadRunnable)
        while runnable is not None:
            try:
                runnable.run()
                self.tm._remove_runnable(self._thread_prog_name, runnable)
            except Exception as error:
                log_execution().exception(error)
            runnable = self.tm._provide_runnable(self._thread_prog_name)
        self.tm._on_thread_shutdown(self._thread_prog_name)