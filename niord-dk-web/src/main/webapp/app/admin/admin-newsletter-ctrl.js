/*
 * Copyright 2016 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * The admin controllers.
 */
angular.module('niord.admin')

    /**
     * ********************************************************************************
     * NewsletterAdminCtrl
     * ********************************************************************************
     * Newsletter Admin Controller
     * Controller for the Admin Newsletter page
     */
    .controller('NewsletterAdminCtrl', ['$scope', '$rootScope', '$uibModal', 'growl', 'LangService',
        'AdminNewsletterService', 'DialogService',
        function ($scope, $rootScope, $uibModal, growl, LangService,
                  AdminNewsletterService, DialogService) {
            'use strict';

            $scope.newsletter = undefined; // The newsletter being edited
            $scope.editMode = 'add';
            $scope.hasRole = $rootScope.hasRole;
            $scope.timeZones = moment.tz.names();


            /** Displays the error message */
            $scope.displayError = function (errorMsg) {
                growl.error("Error: " + errorMsg + " Failed sending newsletter. See console for details.", { ttl: 5000 });
            };

            /** reset the newsletter from the back-end */
            $scope.resetNewsletter = function() {
                $scope.newsletter = undefined;
                $scope.editMode = undefined;
            };

            $scope.addNewsletter = function (isTest) {
                $scope.editMode = 'add';
                $scope.newsletter = {
                    email: undefined,
                    linkChartWeek: undefined,
                    linkChartYear: undefined,
                    isTest: isTest // true or false depending on if we are sending a test newsletter
                };
            };

            $scope.sendNewsLetter = function () {
                if($scope.newsletter.isTest == false || $scope.newsletter.isTest == true &&  $scope.newsletter.email !== undefined) {
                    if($scope.newsletter.linkChartYear !== undefined && $scope.newsletter.linkChartWeek !== undefined) {
                        DialogService.showConfirmDialog(
                            "Send " + ($scope.newsletter.isTest == true ? "test" : "") + " newsletter?", "Send newsletter to " + ($scope.newsletter.isTest == true ? "email" : "mail list") +"?")
                        .then(function() {
                                   AdminNewsletterService.sendNewsletter($scope.newsletter)
                                .success(function(data)
                                {
                                    if(data.status !== 200)
                                    {
                                        $scope.displayError(data.status)
                                        console.log(data)
                                    }
                                    else
                                    {
                                        $scope.resetNewsletter()
                                    }
                                });
                        });
                    }
                }
            };

        }])
