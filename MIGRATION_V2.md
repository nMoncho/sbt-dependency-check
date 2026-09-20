# Migration Guide from v1.x

To migrate your project from v1.x to v2.x: 

1. All `dependencyCheck` tasks, like `dependencyCheckAggregate`, have been unified under one task.


## Single Task

All dependency check tasks have been unified under `dependencyCheck`. We need to use arguments o get the same behavior
as the previously available tasks:

- `dependencyCheckAggregate`: Use `dependencyCheck single-report`
- `dependencyCheckAllProjects`: Use `dependencyCheck single-report all-projects`
