import json, os
from common.context import Service
from common import utils
import logging

class SettingManager(Service):

    def __init__(self):
        super(SettingManager, self).__init__(SettingManager)
        self._model = None
        self._read_configuration()

    def _read_configuration(self):
        if os.path.isfile('config.user.json'):
            config_file = open('config.user.json')
            self._model = json.load(config_file)
            config_file.close()
        config_file = open('config.json')
        default_model = json.load(config_file)
        config_file.close()
        if self._model:
            user_model = self._model
            for key, value in default_model.items():
                if not(key in user_model):
                    user_model[key] = value
        else:
            self._model = default_model

    def setting(self, setting_id):
        answer = self._model.get(setting_id)
        # Do not minimize this since there are false/true settings
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

    def task_dir(self):
        path = self._model["task_dir"]
        if not os.path.exists(path):
             os.makedirs(path)
        return path