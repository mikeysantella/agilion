docker run --rm -v `pwd`:/tmp/sphinx --name sphinx tsgkadot/sphinx-plantuml sphinx-build -b html /tmp/sphinx/source /tmp/sphinx/build
#[ "$1" ] && docker run --rm -ti -v `pwd`:/tmp/sphinx --name sphinx tsgkadot/sphinx-plantuml /tmp/sphinx/source/sphinxtogithub.py /tmp/sphinx/build
# To get a shell, run: docker run --rm -ti -v `pwd`:/tmp/sphinx --name sphinx tsgkadot/sphinx-plantuml sh
