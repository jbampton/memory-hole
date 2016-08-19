(ns yuggoth.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [yuggoth.routes.services.issues :as issues]
            [yuggoth.routes.services.auth :as auth]))

(defapi public-service-routes
  {:swagger {:ui   "/swagger-ui-public"
             :spec "/swagger-public.json"
             :data {:info {:version     "1.0.0"
                           :title       "Public services"
                           :description "Public service operations"}}}}
  (context "/api" []
    :tags ["public"]
    (POST "/login" req
      :return auth/LoginResponse
      :body-params [userid :- String, pass :- String]
      :summary "User login handler"
      (auth/login userid pass req))))

(defapi service-routes
  {:swagger {:ui   "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version     "1.0.0"
                           :title       "Sample API"
                           :description "Sample Services"}}}}
  (context "/api" []
    :tags ["private"]

    (POST "/logout" []
      :return auth/LogoutResponse
      :summary "remove the user from the session"
      (auth/logout))

    (GET "/recent-issues" []
      :return issues/IssueSummaryResults
      :summary "list 10 most recent issues"
      (issues/recent-issues 10))

    (GET "/issues-by-views/:offset/:limit" []
      :path-params [offset :- s/Int limit :- s/Int]
      :return issues/IssueSummaryResults
      :summary "list issues by views using the given offset and limit"
      (issues/issues-by-views {:offset offset :limit limit}))

    (GET "/issues-by-tag/:tag" []
      :path-params [tag :- s/Str]
      :return issues/IssueSummaryResults
      :summary "list issues by the given tag"
      (issues/issues-by-tag {:tag tag}))

    (DELETE "/issue/:id" []
      :path-params  [id :- s/Int]
      :return s/Int
      :summary "delete the issue with the given id"
      (issues/delete-issue! {:support-issue-id id}))

    (POST "/search-issues" []
      :body-params  [query :- s/Str limit :- s/Int offset :- s/Int]
      :return issues/IssueSummaryResults
      :summary "search for issues matching the query"
      (issues/search-issues {:query query
                             :limit limit
                             :offset offset}))

    (GET "/issue/:id" []
      :path-params [id :- s/Int]
      :return issues/IssueResult
      :summary "list 10 most recent issues"
      (issues/issue {:support-issue-id id}))

    (POST "/issue" {:keys [session]}
      :body-params [title :- String
                    summary :- String
                    detail :- String]
      :return s/Num
      :summary "adds a new issue"
      (issues/add-issue!
        {:title   title
         :summary summary
         :detail  detail
         :user-id (-> session :identity :id)}))

    (PUT "/issue" {:keys [session]}
      :body-params [support-issue-id :- s/Num
                    title :- String
                    summary :- String
                    detail :- String]
      :return s/Num
      :summary "update an new issue"
      (issues/update-issue!
        {:support-issue-id support-issue-id
         :title            title
         :summary          summary
         :detail           detail
         :user-id          (-> session :identity :id)}))

    ))
