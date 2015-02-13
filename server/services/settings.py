import json
from common.context import Service
from common import utils
import logging

class SettingManager(Service):

    def __init__(self):
        super(SettingManager, self).__init__(SettingManager)
        self._model = None
        self._read_configuration()

    def _read_configuration(self):
        json_data = open('config.json')
        self._model = json.load(json_data)
        json_data.close()

    def setting(self, setting_id):
        answer = self._model.get(setting_id)
        if answer is not None:
            return answer
        else:
            raise ValueError("No setting with:"+setting_id)

    def log_level(self, setting_id):
        answer = self._model.get(setting_id)
        if answer:
            return getattr(logging, answer)
        else:
            raise ValueError("No setting with:"+setting_id)


    def specified_places(self):
        places = self._model["places"]
        answer_list = []
        for place in places:
            place_object = utils.Object()
            place_object.id = place["id"]
            place_object.folder = place["folder"]
            answer_list.append(place_object)
        return answer_list