
/**
 * The site admin functionality.
 */
angular.module('niord.admin')

    /**
     * Legacy NW import Controller
     */
    .controller('NwIntegrationCtrl', ['$scope', '$rootScope', '$http', 'growl',
        function ($scope, $rootScope, $http, growl) {
            'use strict';

            $scope.legacyNwResult = '';

            /** Displays the error message */
            $scope.displayError = function (err) {
                growl.error("Error");
                $scope.legacyNwResult = 'Error:\n' + err;
            };

            $scope.data = {
                seriesId: undefined,
                tagName: ''
            };
            $scope.tagData = {
                tag: undefined
            };

            // Load the default parameters
            $http.get('/rest/import/nw/params')
                .success(function (result) {
                    $scope.data = result;

                    $scope.tagData.tag = undefined;
                    if (result && result.tagName) {
                        $http.get('/rest/tags/tag/' + result.tagName).then(function(response) {
                            if (response.data && response.data.length > 0) {
                                $scope.tagData.tag = response.data[0];
                            }
                        });
                    }
                });


            /** Tests the legacy NW database connection */
            $scope.testConnection = function() {
                $scope.legacyNwResult = 'Trying to connect...';
                $http.get('/rest/import/nw/test-connection')
                    .success(function (result) {
                        $scope.legacyNwResult = 'Connection status: ' + result;
                    })
                    .error($scope.displayError);

            };

            // Determine the message series for the current domain
            $scope.messageSeriesIds = [];
            if ($rootScope.domain && $rootScope.domain.messageSeries) {
                angular.forEach($rootScope.domain.messageSeries, function (series) {
                    if (series.mainType == 'NW') {
                        $scope.messageSeriesIds.push(series.seriesId);
                    }
                });
            }


            // Sync the tagData.tag with the data.tagName
            $scope.$watch("tagData", function () {
                $scope.data.tagName = $scope.tagData.tag ? $scope.tagData.tag.tagId : undefined;
            }, true);


            /** Imports the legacy NW messages */
            $scope.importLegacyNw = function () {
                $scope.legacyNwResult = 'Start import of legacy MW messages';

                $http.post('/rest/import/nw/import-nw', $scope.data)
                    .success(function (result) {
                        $scope.legacyNwResult = result;
                    })
                    .error($scope.displayError);
            }

        }])


    /**
     * Legacy Firing Area import Controller
     */
    .controller('FaIntegrationCtrl', ['$scope', '$rootScope', '$http', 'growl',
        function ($scope, $rootScope, $http, growl) {
            'use strict';

            $scope.legacyFaResult = '';
            $scope.tagData = { tag: undefined };

            // Determine the message series for the current domain
            $scope.messageSeriesIds = [];
            if ($rootScope.domain && $rootScope.domain.messageSeries) {
                angular.forEach($rootScope.domain.messageSeries, function (series) {
                    if (series.mainType == 'NM') {
                        $scope.messageSeriesIds.push(series.seriesId);
                    }
                });
            }
            $scope.data = {
                seriesId: $scope.messageSeriesIds.length == 1 ? $scope.messageSeriesIds[0] : undefined,
                tagName: ''
            };

            /** Displays the error message */
            $scope.displayError = function (data, status) {
                var error = "Error (code " + status + ")";
                growl.error(error);
                $scope.legacyFaResult = error;
            };

            /** Tests the legacy NW database connection - also used for firing area imports */
            $scope.testConnection = function() {
                $scope.legacyFaResult = 'Trying to connect...';
                $http.get('/rest/import/nw/test-connection')
                    .success(function (result) {
                        $scope.legacyFaResult = 'Connection status: ' + result;
                    })
                    .error($scope.displayError);

            };

            /** Imports the legacy firing areas */
            $scope.importLegacyFa = function () {
                $scope.legacyFaResult = 'Start import of legacy firing areas';

                $http.post('/rest/import/nw/import-fa')
                    .success(function (result) {
                        $scope.legacyFaResult = result;
                    })
                    .error($scope.displayError);
            };


            /** Generates message templates for all firing areas */
            $scope.generateFaTemplates = function () {
                $scope.legacyFaResult = 'Start generating firing area template messages';

                $http.post('/rest/import/nw/generate-fa-messages', $scope.data)
                    .success(function (result) {
                        $scope.legacyFaResult = result;
                    })
                    .error($scope.displayError);
            }

        }])



    /**
     * Legacy NM import Controller
     */
    .controller('NmIntegrationCtrl', ['$scope', '$rootScope', '$http', 'growl',
        function ($scope, $rootScope, $http, growl) {
            'use strict';

            $scope.nmImportUrl = '/rest/import/nm/import-nm';
            $scope.legacyNmResult = '';

            /** Displays the error message */
            $scope.displayError = function (err) {
                growl.error("Error");
                $scope.legacyNmResult = 'Error:\n' + err;
            };


            // Determine the message series for the current domain
            $scope.messageSeriesIds = [];
            if ($rootScope.domain && $rootScope.domain.messageSeries) {
                angular.forEach($rootScope.domain.messageSeries, function (series) {
                    if (series.mainType == 'NM') {
                        $scope.messageSeriesIds.push(series.seriesId);
                    }
                });
            }

            $scope.tagData = { tag: undefined };
            $scope.data = {
                seriesId: $scope.messageSeriesIds.length == 1 ? $scope.messageSeriesIds[0] : undefined,
                tagName: ''
            };


            // Sync the tagData.tag with the data.tagName
            $scope.$watch("tagData", function () {
                $scope.data.tagName = $scope.tagData.tag ? $scope.tagData.tag.tagId : undefined;
            }, true);


            /** Called when the NM html file has been imported */
            $scope.nmFileUploaded = function(result) {
                $scope.legacyNmResult = result;
                $scope.$$phase || $scope.$apply();
            };

            /** Called when the NM html import has failed */
            $scope.nmFileUploadError = function(status) {
                $scope.legacyNmResult = "Error importing NMs (error " + status + ")";
                $scope.$$phase || $scope.$apply();
            };

        }])


    /**
     * Aton Import Controller
     */
    .controller('AtonIntegrationCtrl', ['$scope',
        function ($scope) {
            'use strict';

            $scope.atonUploadUrl = '/rest/import/atons/upload-xls';
            $scope.importResult = '';

            $scope.xlsFileUploaded = function(result) {
                $scope.importResult = result;
                $scope.$$phase || $scope.$apply();
            };

            $scope.xlsFileUploadError = function(status) {
                $scope.importResult = "Error importing AtoNs (error " + status + ")";
                $scope.$$phase || $scope.$apply();
            };

        }]);
