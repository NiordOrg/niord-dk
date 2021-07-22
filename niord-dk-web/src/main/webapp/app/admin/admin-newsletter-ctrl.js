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

            $scope.newsletterList = [];
            $scope.newsletter = undefined; // The newsletter being edited
            $scope.testNewsletter = undefined; // true or false depending on if we are sending a test newsletter
            $scope.editMode = 'add';
            $scope.search = '';
            $scope.hasRole = $rootScope.hasRole;
            $scope.timeZones = moment.tz.names();

            /** reset the newsletter from the back-end */
            $scope.resetNewsletter = function() {
                $scope.newsletter = undefined;
                $scope.testNewsletter = undefined;
            };

            $scope.addNewsletter = function (isTest) {
                $scope.editMode = 'add';
                $scope.testNewsletter = isTest;
                $scope.newsletter = {
                    email: undefined,
                    linkChartWeek: undefined,
                    linkChartYear: undefined,
                };
            };

            $scope.sendNewsLetter = function () {
                console.log($scope.newsletter);
                if($scope.testNewsletter == false || $scope.testNewsletter == true &&  $scope.newsletter.email !== undefined) {
                    if($scope.newsletter.linkChartYear !== undefined && $scope.newsletter.linkChartWeek !== undefined) {
                        DialogService.showConfirmDialog(
                            "Send " + ($scope.testNewsletter == true ? "test" : "") + " newsletter?", "Send newsletter to " + ($scope.testNewsletter == true ? "email" : "mail list") +"?")
                        .then(function() {
                                if($scope.testNewsletter == false)
                                {
                                    $scope.newsletter.email  = ""
                                }
                               AdminNewsletterService.sendNewsletter($scope.newsletter)
                                .success(function (newsletter) {
                                    $scope.newsletter = newsletter;
                                });

                        });
                    }
                }
            };
        }])
