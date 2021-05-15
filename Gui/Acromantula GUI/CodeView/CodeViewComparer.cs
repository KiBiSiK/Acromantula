using System;
using System.Windows.Forms;

namespace Acromantula_GUI.CodeView
{
    public partial class CodeViewComparer : Form
    {
        public CodeViewComparer()
        {
            InitializeComponent();
            scintillaDiffControl1.IsEntireLineHighlighted = true;
        }

        private void scintillaDiffControl1_Load(object sender, EventArgs e)
        {
            scintillaDiffControl1.TextLeft = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,\n sed diam nonumy eirmod tempor invidunt ut labore et dolore magna \naliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum.\n Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\n Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat,\n sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren,\n no sea takimata sanctus est Lorem ipsum dolor sit amet.";
            scintillaDiffControl1.TextRight = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr,\n sed daim nmunoy eomrid topmer inudivnt ut lrobae et droloe mngaa \nalyuqiam etar, sed daim voutpula. At vreo eos et acsucam et jtsuo duo doroles et ea rmube.\n Sett ctila ksad gubrgreen, no sea taamikta satcnus est Lerom iuspm dolor sit atem.\n Lerom iuspm dolor sit atem, conetestur sadcspiing ertil, sed daim nmunoy eomrid topmer inudivnt ut lrobae et droloe mngaa alyuqiam etar,\n sed daim voutpula. At vreo eos et acsucam et jtsuo duo doroles et ea rmube. Sett ctila ksad gubrgreen,\n no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        }
    }
}
