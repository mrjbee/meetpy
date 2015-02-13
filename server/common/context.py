
class Context(object):
    def __init__(self):
        super(Context, self).__init__()
        self._serviceMap = {}
        self._featureMap = {}

    def load(self):
        self.configure()
        for kev, value in self._serviceMap.items():
            value.init()

    def configure(self):
        pass

    def required_service(self, instance):
        self._serviceMap[instance.my_class()] = instance
        instance._context = self

    def required_feature(self, instance):
        self._featureMap[instance.my_class()] = instance
        instance.services = self._serviceMap
        instance._context = self

    def get_feature(self, feature_class):
        return self._get(self._featureMap, feature_class)

    def get_service(self, service_class):
        return self._get(self._serviceMap, service_class)

    def _get(self, lookup_map, lookup_class):
        answer_item = lookup_map.get(lookup_class, None)
        if answer_item is None:
            raise ValueError("No item with class = " + lookup_class)
        return answer_item


class RequestExecutor(object):

    def __init__(self, context):
        super(RequestExecutor, self).__init__()
        assert isinstance(context, Context)
        self._context = context

    def execute(self, feature_class, request=None):
        feature = self._context.get_feature(feature_class)
        return feature.execute(request)


class ContextItem(object):
    def __init__(self, impl_class):
        super(ContextItem, self).__init__()
        self._impl_class = impl_class
        self._context = None

    def my_class(self):
        return self._impl_class

    def service(self, service_class):
        config = self._context
        service = config.get_service(service_class)
        return service


class Feature(ContextItem):
    def __init__(self, feature_class):
        super(Feature, self).__init__(feature_class)

    def execute(self, args=None):
        return None


class Service(ContextItem):
    def __init__(self, service_class):
        super(Service, self).__init__(service_class)

    def init(self):
        pass