from common.context import Feature
from services import services_settings
from common import utils
import os


class ExploreFiles(Feature):
    def __init__(self):
        super(ExploreFiles, self).__init__(ExploreFiles)

    def execute(self, parent_path=None):
        if os.path.exists(parent_path) is False:
            return None
        if os.path.isfile(parent_path) is True:
            return []
        else:
            child_list = os.listdir(parent_path)
            answer = []
            for child in child_list:
                child_object = utils.Object()
                child_object.path = os.path.join(parent_path, child)
                child_object.is_file = os.path.isfile(child_object.path)
                child_object.size = os.path.getsize(child_object.path)
                answer.append(child_object)
        return answer


class GetAvailablePlaces(Feature):

    def __init__(self):
        super(GetAvailablePlaces, self).__init__(GetAvailablePlaces)

    def execute(self, args=None):
        sm = self.service(services_settings.SettingManager)
        assert isinstance(sm, services_settings.SettingManager)
        places = sm.specified_places()
        answer_list = []
        for place in places:
            place_result = utils.Object()
            place_result.name = place.id
            place_result.folder = place.folder
            answer_list.append(place_result)
        return answer_list