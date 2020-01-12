module App = {
  module Result = Belt.Result;
  type action =
    | SyncRoute(Router.route);

  type state = {route: Router.route};

  let (initialRoute, payload) = Router.getInitialRoute();

  let initialState = {route: initialRoute};

  [@bs.val] external atob: string => string = "";

  let decode = (s: string) =>
    atob(Js.String.replace("_", "=", Js.String.replace("_", "=", s)));

  let parseQuery = (search: string) => {
    open Belt;

    let emptyState = IssueForm.FormConfig.initialState(None);

    let toPriority =
      fun
      | "high" => IssueForm.FormConfig.High
      | "low" => IssueForm.FormConfig.Low
      | _ => IssueForm.FormConfig.Medium;

    Js.String.split("&", search)
    ->Array.reduce(
        emptyState,
        (acc, part) => {
          let [k, v] = Js.String.split("=", part)->List.fromArray;

          switch (k) {
          | "precondition" => {...acc, precondition: decode(v)}
          | "expected" => {...acc, expected: decode(v)}
          | "actual" => {...acc, actual: decode(v)}
          | "priority" => {...acc, priority: decode(v)->toPriority}
          | "partOfApp" => {...acc, partOfApp: decode(v)}
          | "name" => {...acc, name: decode(v)}
          | "description" => {...acc, additional: decode(v)}
          | "errorDate" => {...acc, errorDate: decode(v)}
          | _ => acc
          };
        },
      );
  };

  [@react.component]
  let make = () => {
    let (currentState, setState) = React.useState(_ => initialState);
    /* let changeRoute = route => { */
    /*   Router.go(route); */
    /*   setState(_ => {route: route}); */
    /* }; */

    <>
      {switch (currentState.route) {
       | Issue(query) =>
         let state = parseQuery(query);
         Js.log(state);
         <IssueForm state />;
       | _ => <div />
       }}
    </>;
  };
};

ReactDOMRe.renderToElementWithId(<App />, "root");
